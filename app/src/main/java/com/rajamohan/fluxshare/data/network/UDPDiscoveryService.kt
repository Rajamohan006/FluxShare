package com.rajamohan.fluxshare.data.network

import android.content.Context
import android.net.wifi.WifiManager
import com.rajamohan.fluxshare.domain.model.Device
import com.rajamohan.fluxshare.domain.model.DiscoveryBroadcast
import com.rajamohan.fluxshare.domain.model.TransferConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException

@OptIn(InternalSerializationApi::class)
class UDPDiscoveryService(
    private val context: Context,
    private val deviceId: String,
    private val deviceName: String,
    private val port: Int = TransferConstants.DEFAULT_PORT
) {
    private val discoveryPort = TransferConstants.DISCOVERY_PORT // UDP discovery port (8889)
    private var listenerSocket: DatagramSocket? = null
    private var broadcastSocket: DatagramSocket? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    private val json = Json { ignoreUnknownKeys = true }

    private val _discoveredDevices = MutableStateFlow<List<Device>>(emptyList())
    val discoveredDevices: StateFlow<List<Device>> = _discoveredDevices.asStateFlow()

    private val deviceMap = mutableMapOf<String, Device>()
    private var isDiscovering = false

    @OptIn(DelicateCoroutinesApi::class)
    fun startDiscovery(scope: CoroutineScope) {
        if (isDiscovering) {
            Timber.w("Discovery already running")
            return
        }
        isDiscovering = true
        Timber.d("⚡ Starting discovery...")

        acquireMulticastLock()

        // create and reuse broadcast socket
        try {
            broadcastSocket = DatagramSocket().apply {
                broadcast = true
                reuseAddress = true
            }
            Timber.d("Broadcast socket created")
        } catch (e: Exception) {
            Timber.e(e, "Failed to create broadcast socket")
        }

        scope.launch(Dispatchers.IO) {
            try {
                startBroadcasting(this)
                startListening(this)
                startCleanupTask(this)
            } catch (e: Exception) {
                Timber.e(e, "Discovery error")
                isDiscovering = false
            }
        }
    }

    private fun acquireMulticastLock() {
        try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            multicastLock = wm.createMulticastLock("fluxshare_multicast_lock").apply {
                setReferenceCounted(true)
                acquire()
            }
            Timber.d("Multicast lock acquired")
        } catch (e: Exception) {
            Timber.e(e, "Failed to acquire multicast lock")
        }
    }

    private fun releaseMulticastLock() {
        try {
            multicastLock?.let {
                if (it.isHeld) it.release()
                multicastLock = null
                Timber.d("Multicast lock released")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to release multicast lock")
        }
    }

    private suspend fun startBroadcasting(scope: CoroutineScope) {
        scope.launch {
            while (isActive && isDiscovering) {
                try {
                    val broadcast = DiscoveryBroadcast(
                        deviceId = deviceId,
                        deviceName = deviceName,
                        port = port // This is TCP port peers should connect to (8888)
                    )
                    val message = json.encodeToString(broadcast)
                    sendBroadcast(message)
                } catch (e: Exception) {
                    Timber.e(e, "Broadcast error")
                }
                delay(TransferConstants.DISCOVERY_INTERVAL_MS)
            }
        }
    }

    private fun sendBroadcast(message: String) {
        try {
            // try to reuse broadcastSocket; fallback to a temporary socket if needed
            val socket = broadcastSocket ?: DatagramSocket().also {
                it.broadcast = true
                it.reuseAddress = true
            }

            val data = message.toByteArray()
            var broadcastAddress = getBroadcastAddress()

            // If network unreachable try the global broadcast as last resort
            val packet = DatagramPacket(data, data.size, broadcastAddress, discoveryPort)
            try {
                socket.send(packet)
                Timber.v("Broadcast sent to ${broadcastAddress.hostAddress}:$discoveryPort")
            } catch (e: Exception) {
                Timber.w(e, "Send failed to ${broadcastAddress.hostAddress}, trying 255.255.255.255")
                // fallback
                broadcastAddress = InetAddress.getByName("255.255.255.255")
                val fallback = DatagramPacket(data, data.size, broadcastAddress, discoveryPort)
                socket.send(fallback)
                Timber.v("Fallback broadcast sent to 255.255.255.255:$discoveryPort")
            }

            // If we created a temp socket, close it
            if (broadcastSocket == null) socket.close()
        } catch (e: Exception) {
            Timber.e(e, "Failed to send broadcast")
        }
    }

    private suspend fun startListening(scope: CoroutineScope) {
        scope.launch {
            try {
                listenerSocket = DatagramSocket(null).apply {
                    reuseAddress = true
                    // bind explicitly to discovery port and all interfaces
                    bind(InetSocketAddress(discoveryPort))
                    soTimeout = 2000 // 2 seconds so loop stays responsive
                }

                Timber.d("UDP listener started on port $discoveryPort")
                val buffer = ByteArray(2048)

                while (isActive && isDiscovering) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        listenerSocket?.receive(packet) // may throw SocketTimeoutException or SocketException

                        val message = String(packet.data, 0, packet.length)
                        val senderIp = packet.address.hostAddress ?: ""

                        handleDiscoveryMessage(message, senderIp)
                    } catch (e: SocketTimeoutException) {
                        // expected, loop to check isDiscovering
                    } catch (e: SocketException) {
                        // socket closed or network error - if stopping, break quietly
                        if (!isDiscovering) {
                            Timber.d("Listener socket closed while stopping")
                            break
                        } else {
                            Timber.e(e, "Listener socket exception")
                            // optional: attempt to recreate listener or sleep briefly before retry
                            break
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Listener error")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start UDP listener")
            }
        }
    }

    private fun handleDiscoveryMessage(message: String, ipAddress: String) {
        try {
            val broadcast = json.decodeFromString<DiscoveryBroadcast>(message)

            if (broadcast.deviceId == deviceId) {
                return // Ignore own broadcasts
            }

            val device = Device(
                id = broadcast.deviceId,
                name = broadcast.deviceName,
                ipAddress = ipAddress,
                port = broadcast.port, // TCP port that peer announced
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )

            val existingDevice = deviceMap[device.id]
            if (existingDevice == null) {
                Timber.d("✓ New device: ${device.name} @ $ipAddress:${device.port}")
            }

            deviceMap[device.id] = device
            _discoveredDevices.value = deviceMap.values.toList()

        } catch (e: Exception) {
            Timber.v("Invalid discovery message: ${e.message}")
        }
    }

    private suspend fun startCleanupTask(scope: CoroutineScope) {
        scope.launch {
            while (isActive && isDiscovering) {
                delay(10_000)

                val now = System.currentTimeMillis()
                val timeout = TransferConstants.PEER_TIMEOUT_MS

                val removed = deviceMap.entries.removeAll { (_, device) ->
                    (now - device.lastSeen) > timeout
                }

                if (removed) {
                    _discoveredDevices.value = deviceMap.values.toList()
                    Timber.d("Cleaned up offline devices")
                }
            }
        }
    }

    fun stopDiscovery() {
        isDiscovering = false
        try {
            listenerSocket?.close()
            broadcastSocket?.close()
            releaseMulticastLock()
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop discovery")
        }
        deviceMap.clear()
        _discoveredDevices.value = emptyList()
        Timber.d("Discovery stopped")
    }

    private fun getBroadcastAddress(): InetAddress {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val dhcp = wifiManager.dhcpInfo

            if (dhcp != null && dhcp.netmask != 0) {
                val broadcast = (dhcp.ipAddress and dhcp.netmask) or dhcp.netmask.inv()
                val bytes = ByteArray(4)
                for (i in 0..3) {
                    bytes[i] = (broadcast shr (i * 8) and 0xFF).toByte()
                }
                val addr = InetAddress.getByAddress(bytes)
                Timber.d("Calculated broadcast address: ${addr.hostAddress}")
                return addr
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get broadcast address")
        }

        // fallback
        return InetAddress.getByName("255.255.255.255")
    }
}
