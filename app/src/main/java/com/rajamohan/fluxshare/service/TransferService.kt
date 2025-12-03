package com.rajamohan.fluxshare.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.rajamohan.fluxshare.MainActivity
import com.rajamohan.fluxshare.data.network.TCPTransferHandler
import com.rajamohan.fluxshare.domain.model.*
import com.rajamohan.fluxshare.domain.repository.DeviceRepository
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import com.rajamohan.fluxshare.domain.usecase.ReceiveFileUseCase
import com.rajamohan.fluxshare.domain.usecase.SendFileUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.InternalSerializationApi
import timber.log.Timber
import java.io.File
import java.net.Socket
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@AndroidEntryPoint
class TransferService : Service() {
    @Inject lateinit var transferRepository: TransferRepository
    @Inject lateinit var tcpHandler: TCPTransferHandler
    @Inject lateinit var sendFileUseCase: SendFileUseCase
    @Inject lateinit var receiveFileUseCase: ReceiveFileUseCase
    @Inject lateinit var deviceRepository: DeviceRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeTransfers = mutableMapOf<String, Job>()
    private val receivingTransfers = mutableMapOf<String, ReceiveTransferState>()
    private var serverJob: Job? = null
    private var isServerRunning = false

    // Track receive state
    private data class ReceiveTransferState(
        val transferId: String,
        val fileName: String,
        val fileSize: Long,
        val totalChunks: Int,
        val receivedChunks: MutableSet<Int> = mutableSetOf(),
        val filePath: String
    )

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "fluxshare_transfers"
        private const val NOTIFICATION_ID = 1001
        private const val DEFAULT_PORT = 8888

        fun startServer(context: Context) {
            Timber.d("startServer() called")
            val intent = Intent(context, TransferService::class.java).apply {
                action = "START_SERVER"
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start service")
            }
        }

        fun startTransfer(context: Context, transferId: String) {
            Timber.d("startTransfer() called for: $transferId")
            val intent = Intent(context, TransferService::class.java).apply {
                action = "START_TRANSFER"
                putExtra("transfer_id", transferId)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start transfer")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("═══════════════════════════════════════")
        Timber.d("TransferService onCreate()")
        Timber.d("═══════════════════════════════════════")

        createNotificationChannel()

        try {
            startForeground(NOTIFICATION_ID, createNotification("FluxShare Ready", 0f))
            Timber.d("✓ Foreground service started")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start foreground service")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: "null"
        Timber.d("═══════════════════════════════════════")
        Timber.d("onStartCommand() - Action: $action")
        Timber.d("═══════════════════════════════════════")

        when (action) {
            "START_SERVER" -> {
                Timber.d("➤ Starting TCP Server...")
                startTCPServer()
            }
            "START_TRANSFER" -> {
                val transferId = intent?.getStringExtra("transfer_id")
                transferId?.let { handleTransfer(it) }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTCPServer() {
        if (isServerRunning) {
            Timber.w("TCP Server already running!")
            return
        }

        serverJob = serviceScope.launch {
            try {
                isServerRunning = true
                Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Timber.d("✓ TCP SERVER STARTING ON PORT $DEFAULT_PORT")
                Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                tcpHandler.startServer(port = DEFAULT_PORT) { message, socket ->
                    Timber.d("⬇ Received: ${message::class.simpleName}")
                    handleIncomingMessage(message, socket)
                }
            } catch (e: Exception) {
                isServerRunning = false
                Timber.e(e, "✗ TCP Server error!")
            }
        }

        Timber.d("TCP Server job created: ${serverJob?.isActive}")
    }

    private fun handleTransfer(transferId: String) {
        if (activeTransfers.containsKey(transferId)) {
            Timber.w("Transfer $transferId already active")
            return
        }

        val job = serviceScope.launch {
            try {
                val transfer = transferRepository.getTransferById(transferId).first()
                if (transfer == null) {
                    Timber.e("Transfer not found: $transferId")
                    return@launch
                }

                Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Timber.d("Starting: ${transfer.fileName}")
                Timber.d("Direction: ${transfer.direction}")
                Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                when (transfer.direction) {
                    TransferDirection.SEND -> executeSendTransfer(transfer)
                    TransferDirection.RECEIVE -> executeReceiveTransfer(transfer)
                }
            } catch (e: Exception) {
                Timber.e(e, "Transfer error: $transferId")
                transferRepository.failTransfer(transferId, e.message ?: "Unknown error")
            } finally {
                activeTransfers.remove(transferId)
            }
        }

        activeTransfers[transferId] = job
    }

    @OptIn(InternalSerializationApi::class)
    private suspend fun executeSendTransfer(transfer: TransferEntity) {
        Timber.d("⬆ Sending: ${transfer.fileName}")
        transferRepository.updateTransferState(transfer.id, TransferState.CONNECTING)

        // Resolve device
        val devices = deviceRepository.discoveredDevices.first()
        val targetDevice = devices.find { it.id == transfer.peerId }

        if (targetDevice == null) {
            Timber.e("✗ Target device not found")
            transferRepository.failTransfer(transfer.id, "Device not found")
            return
        }

        Timber.d("⚡ Connecting to ${targetDevice.ipAddress}:${targetDevice.port}...")
        val socket = tcpHandler.connectToPeer(targetDevice.ipAddress, targetDevice.port)

        if (socket == null) {
            Timber.e("✗ Connection FAILED!")
            transferRepository.failTransfer(transfer.id, "Connection failed")
            return
        }

        Timber.d("✓ Connected!")

        try {
            transferRepository.updateTransferState(transfer.id, TransferState.TRANSFERRING)
            updateNotification("Sending ${transfer.fileName}", 0.0f)

            // Send file offer
            val fileOffer = TransferMessage.FileOffer(
                transferId = transfer.id,
                fileName = transfer.fileName,
                fileSize = transfer.fileSize,
                mimeType = transfer.mimeType,
                sha256Hash = transfer.sha256Hash ?: "",
                totalChunks = transfer.totalChunks,
                chunkSize = TransferConstants.CHUNK_SIZE
            )
            tcpHandler.sendMessage(socket, fileOffer)
            Timber.d("✓ File offer sent")

            // Wait a bit for receiver to accept
            delay(500)

            // Send chunks
            val incompleteChunks = transferRepository.getIncompleteChunks(transfer.id)
            Timber.d("📦 Sending ${incompleteChunks.size} chunks")

            incompleteChunks.forEachIndexed { index, chunk ->
                if (!coroutineContext.isActive) {
                    return@forEachIndexed
                }

                val chunkData = sendFileUseCase.readChunk(
                    filePath = transfer.filePath,
                    chunkIndex = chunk.chunkIndex,
                    chunkSize = chunk.chunkSize
                )

                tcpHandler.sendChunkData(socket, transfer.id, chunk.chunkIndex, chunkData)
                transferRepository.markChunkComplete(transfer.id, chunk.chunkIndex, calculateCRC32(chunkData))

                val progress = (index + 1).toFloat() / incompleteChunks.size
                updateNotification("Sending ${transfer.fileName}", progress)

                Timber.d("✓ Chunk ${chunk.chunkIndex + 1}/${transfer.totalChunks} sent")
            }

            // Send completion message
            val completeMsg = TransferMessage.TransferComplete(transferId = transfer.id)
            tcpHandler.sendMessage(socket, completeMsg)

            transferRepository.completeTransfer(transfer.id)
            showCompletionNotification("✓ Sent: ${transfer.fileName}", true)

            Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Timber.d("✓✓✓ SEND COMPLETE ✓✓✓")
            Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        } catch (e: Exception) {
            Timber.e(e, "✗ Send failed")
            transferRepository.failTransfer(transfer.id, e.message ?: "Transfer failed")
            showCompletionNotification("✗ Failed: ${transfer.fileName}", false)
        } finally {
            socket.close()
        }
    }

    private suspend fun executeReceiveTransfer(transfer: TransferEntity) {
        // Receive is handled by handleIncomingMessage
        Timber.d("⬇ Receive transfer set up for: ${transfer.fileName}")
    }

    // ========== KEY METHOD - Handle Incoming Messages ==========
    @OptIn(InternalSerializationApi::class)
    private suspend fun handleIncomingMessage(message: TransferMessage, socket: Socket) {
        try {
            when (message) {
                is TransferMessage.FileOffer -> {
                    Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Timber.d("📥 INCOMING FILE OFFER")
                    Timber.d("File: ${message.fileName}")
                    Timber.d("Size: ${message.fileSize} bytes")
                    Timber.d("Chunks: ${message.totalChunks}")
                    Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Create download directory
                    val downloadsDir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "FluxShare"
                    )
                    downloadsDir.mkdirs()

                    val filePath = File(downloadsDir, message.fileName).absolutePath
                    Timber.d("Save location: $filePath")

                    // Create receive state
                    val receiveState = ReceiveTransferState(
                        transferId = message.transferId,
                        fileName = message.fileName,
                        fileSize = message.fileSize,
                        totalChunks = message.totalChunks,
                        filePath = filePath
                    )
                    receivingTransfers[message.transferId] = receiveState

                    // Create transfer record
                    val transferId = transferRepository.createTransfer(
                        fileName = message.fileName,
                        filePath = filePath,
                        fileSize = message.fileSize,
                        mimeType = message.mimeType,
                        direction = TransferDirection.RECEIVE,
                        peerId = socket.inetAddress.hostAddress ?: "unknown",
                        peerName = "Sender",
                        isEncrypted = false
                    )

                    // Update notification
                    showReceivingNotification(message.fileName, 0f)

                    // Send accept
                    val accept = TransferMessage.FileAccept(
                        transferId = message.transferId,
                        accepted = true
                    )
                    tcpHandler.sendMessage(socket, accept)
                    Timber.d("✓ Sent accept message")
                }

                is TransferMessage.ChunkData -> {
                    val state = receivingTransfers[message.transferId]
                    if (state == null) {
                        Timber.w("Received chunk for unknown transfer: ${message.transferId}")
                        return
                    }

                    Timber.d("📦 Receiving chunk ${message.chunkIndex + 1}/${state.totalChunks}")

                    // Verify CRC
                    val calculatedCRC = calculateCRC32(message.data)
                    if (calculatedCRC != message.crc32) {
                        Timber.e("✗ CRC mismatch! Expected: ${message.crc32}, Got: $calculatedCRC")
                        // Send NAK?
                        return
                    }

                    // Write chunk to file
                    receiveFileUseCase.writeChunk(
                        filePath = state.filePath,
                        chunkIndex = message.chunkIndex,
                        chunkSize = TransferConstants.CHUNK_SIZE,
                        data = message.data
                    )

                    state.receivedChunks.add(message.chunkIndex)
                    transferRepository.markChunkComplete(message.transferId, message.chunkIndex, message.crc32)

                    val progress = state.receivedChunks.size.toFloat() / state.totalChunks
                    showReceivingNotification(state.fileName, progress)

                    Timber.d("✓ Chunk ${message.chunkIndex + 1} saved (${(progress * 100).toInt()}%)")

                    // Send ACK
                    val ack = TransferMessage.ChunkAck(
                        transferId = message.transferId,
                        chunkIndex = message.chunkIndex,
                        success = true
                    )
                    tcpHandler.sendMessage(socket, ack)

                    // Check if complete
                    if (state.receivedChunks.size == state.totalChunks) {
                        Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        Timber.d("✓✓✓ RECEIVE COMPLETE ✓✓✓")
                        Timber.d("File: ${state.fileName}")
                        Timber.d("Location: ${state.filePath}")
                        Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        transferRepository.completeTransfer(message.transferId)
                        showCompletionNotification("✓ Received: ${state.fileName}", true)

                        // Show big success notification
                        showBigNotification(
                            title = "✓ File Received!",
                            message = "${state.fileName}\nSaved to: Downloads/FluxShare/",
                            success = true
                        )

                        receivingTransfers.remove(message.transferId)
                    }
                }

                is TransferMessage.TransferComplete -> {
                    Timber.d("✓ Sender confirmed transfer complete: ${message.transferId}")
                }

                is TransferMessage.FileAccept -> {
                    Timber.d("✓ Receiver accepted file: ${message.transferId}")
                }

                else -> {
                    Timber.d("Unhandled message type: ${message::class.simpleName}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling message")
        }
    }

    private fun calculateCRC32(data: ByteArray): Long {
        val crc = java.util.zip.CRC32()
        crc.update(data)
        return crc.value
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "File Transfers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "File transfer notifications"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, progress: Float): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(if (progress > 0) "${(progress * 100).toInt()}%" else "Ready")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentIntent(pendingIntent)
            .setProgress(100, (progress * 100).toInt(), false)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(fileName: String, progress: Float) {
        val notification = createNotification(fileName, progress)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun showReceivingNotification(fileName: String, progress: Float) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("⬇ Receiving: $fileName")
            .setContentText("${(progress * 100).toInt()}%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, (progress * 100).toInt(), false)
            .setOngoing(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun showCompletionNotification(message: String, success: Boolean) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(message)
            .setSmallIcon(
                if (success) android.R.drawable.stat_sys_download_done
                else android.R.drawable.stat_notify_error
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(message.hashCode(), notification)
    }

    private fun showBigNotification(title: String, message: String, success: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(
                if (success) android.R.drawable.stat_sys_download_done
                else android.R.drawable.stat_notify_error
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify((title + message).hashCode(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("TransferService onDestroy()")
        serviceScope.cancel()
        serverJob?.cancel()
        isServerRunning = false
    }
}


