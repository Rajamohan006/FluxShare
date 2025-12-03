package com.rajamohan.fluxshare.data.network

import android.util.Base64
import com.rajamohan.fluxshare.domain.model.TransferConstants
import com.rajamohan.fluxshare.domain.model.TransferMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import timber.log.Timber
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

@OptIn(InternalSerializationApi::class)
class TCPTransferHandler {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private var serverSocket: ServerSocket? = null

    suspend fun startServer(port: Int, onMessageReceived: suspend (TransferMessage, Socket) -> Unit) = withContext(Dispatchers.IO) {
        try {
            serverSocket = ServerSocket(port).apply {
                reuseAddress = true
                soTimeout = 0
            }

            Timber.d("Server started on port $port")

            while (isActive) {
                try {
                    val socket = serverSocket?.accept() ?: break
                    Timber.d("✓ Client connected from ${socket.inetAddress.hostAddress}")

                    launch {
                        handleClientConnection(socket, onMessageReceived)
                    }
                } catch (e: SocketException) {
                    if (!isActive) break
                    Timber.e(e, "Server socket error")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Server failed to start")
        }
    }

    private suspend fun handleClientConnection(
        socket: Socket,
        onMessageReceived: suspend (TransferMessage, Socket) -> Unit
    ) = withContext(Dispatchers.IO) {
        // Use a buffered input stream to allow mixing line-based and raw reads
        val rawInput = socket.getInputStream().buffered()
        try {
            // Configure read timeout (already done by caller in many places, but safe to set)
            socket.soTimeout = TransferConstants.SOCKET_TIMEOUT_MS

            // Local helper: read a line terminated by '\n' (ignores '\r'), returns null on EOF
            fun readLineFromStream(input: java.io.BufferedInputStream): String? {
                val baos = java.io.ByteArrayOutputStream()
                while (true) {
                    val b = input.read()
                    if (b == -1) {
                        // EOF
                        if (baos.size() == 0) return null
                        break
                    }
                    if (b == '\n'.code) break
                    if (b == '\r'.code) continue
                    baos.write(b)
                    // safety: extremely large lines (>10MB) are likely broken clients
                    if (baos.size() > 10_000_000) {
                        Timber.w("Line too large, truncating at 10MB")
                        break
                    }
                }
                return baos.toString(Charsets.UTF_8.name())
            }

            // Main read loop
            while (true) {
                // Close conditions
                if (socket.isClosed) {
                    Timber.d("Socket closed, stopping handler")
                    break
                }

                // Attempt to read a line (blocks until '\n' or EOF or timeout)
                val line = try {
                    readLineFromStream(rawInput)
                } catch (e: java.net.SocketTimeoutException) {
                    Timber.w("Connection read timed out, closing socket")
                    break
                } catch (e: Exception) {
                    Timber.e(e, "Error reading line from socket")
                    break
                }

                // EOF -> break
                if (line == null) {
                    Timber.d("EOF reached on socket, closing handler")
                    break
                }

                // Skip empty lines
                if (line.isBlank()) continue

                Timber.d("Received: ${line.take(200)}")

                // CHUNK protocol branch (header then raw bytes)
                // --- inside the main read loop, replace your CHUNK handling with this ---
                // --- REPLACE existing "if (line.startsWith("CHUNK:")) { ... }" block with this ---
                if (line.startsWith("CHUNK:")) {
                    try {
                        val header = line.removePrefix("CHUNK:").trim()
                        val parts = header.split(":")
                        if (parts.size < 4) {
                            Timber.w("Invalid CHUNK header (too few parts): $line")
                            continue
                        }

                        val transferId = parts[0]
                        val chunkIndex = parts[1].toIntOrNull()
                        val length = parts[2].toIntOrNull()
                        val crcHeader = parts.getOrNull(3)?.toLongOrNull() ?: -1L

                        if (chunkIndex == null || length == null || length < 0) {
                            Timber.w("Invalid CHUNK header for transfer=$transferId: $line")
                            continue
                        }
                        if (length > 50_000_000) {
                            Timber.w("Rejecting suspicious chunk length=$length transfer=$transferId idx=$chunkIndex")
                            continue
                        }

                        val origTimeout = try { socket.soTimeout } catch (_: Exception) { TransferConstants.SOCKET_TIMEOUT_MS }
                        val increasedTimeout = (origTimeout + length / 10).coerceAtMost(60_000)
                        try { socket.soTimeout = increasedTimeout } catch (_: Exception) {}

                        val dataIn = java.io.DataInputStream(rawInput)
                        val rawData = ByteArray(length)
                        try {
                            dataIn.readFully(rawData) // throws EOFException if truncated
                        } catch (e: java.io.EOFException) {
                            Timber.w("EOF while reading chunk: transferId=$transferId idx=$chunkIndex expected=$length bytes")
                            break
                        } catch (e: java.net.SocketTimeoutException) {
                            Timber.w("SocketTimeout while reading chunk body: transferId=$transferId idx=$chunkIndex")
                            break
                        } catch (e: java.net.SocketException) {
                            Timber.w(e, "SocketException while reading chunk: transferId=$transferId idx=$chunkIndex")
                            break
                        } finally {
                            try { socket.soTimeout = origTimeout } catch (_: Exception) {}
                        }

                        val computedCrc = calculateCRC32(rawData)
                        if (crcHeader != -1L && computedCrc != crcHeader) {
                            Timber.w("CRC mismatch transfer=$transferId idx=$chunkIndex expected=$crcHeader computed=$computedCrc length=$length")
                            // Option: send CHUNK_NACK (see snippet below)
                            continue
                        }

                        Timber.d("Received chunk transferId=$transferId idx=$chunkIndex length=$length crc=$computedCrc")

                        // Persist to .partial (see next snippet), or forward upward:
                        val chunkMsg = TransferMessage.ChunkData(
                            transferId = transferId,
                            chunkIndex = chunkIndex,
                            data = rawData,
                            crc32 = computedCrc
                        )

                        onMessageReceived(chunkMsg, socket)

                        // Optionally send ACK:
                        try {
                            val ack = "CHUNK_ACK:$transferId:$chunkIndex\n"
                            socket.getOutputStream().write(ack.toByteArray(Charsets.UTF_8))
                            socket.getOutputStream().flush()
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to send CHUNK_ACK for transfer=$transferId idx=$chunkIndex")
                            // not fatal; continue
                        }

                        continue
                    } catch (e: Exception) {
                        Timber.e(e, "Error processing CHUNK header/body, closing connection")
                        break
                    }
                }



                // Otherwise treat the line as JSON control message
                try {
                    val message = parseTransferMessage(line)
                    onMessageReceived(message, socket)
                } catch (e: IllegalArgumentException) {
                    Timber.w("Failed to parse message: ${line.take(200)} - ${e.message}")
                    // Heuristic: if remote is speaking HTTP/WebSocket or other non-JSON text, close the connection
                    if (line.startsWith("GET ") || line.startsWith("POST ") ||
                        line.startsWith("Host:") || line.startsWith("Upgrade:") ||
                        line.startsWith("Connection:") || line.startsWith("Sec-WebSocket-")) {
                        Timber.w("Received HTTP/WebSocket handshake, closing connection.")
                        break
                    }
                    // otherwise just continue reading next lines
                } catch (e: Exception) {
                    Timber.e(e, "Unexpected error while parsing control message")
                    break
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Timber.w("Connection read timed out, closing socket")
        } catch (e: Exception) {
            Timber.e(e, "Connection error")
        } finally {
            try {
                socket.close()
                Timber.d("Socket closed")
            } catch (e: Exception) {
                Timber.e(e, "Error closing socket")
            }
        }
    }


    private fun readLineFromStream(input: java.io.BufferedInputStream): String? {
        val baos = java.io.ByteArrayOutputStream()
        while (true) {
            val b = input.read()
            if (b == -1) {
                // EOF
                if (baos.size() == 0) return null
                break
            }
            if (b == '\n'.code) break
            // ignore CR ('\r')
            if (b == '\r'.code) continue
            baos.write(b)
            // safety: avoid runaway lines
            if (baos.size() > 10_000) {
                return baos.toString(Charsets.UTF_8.name())
            }
        }
        return baos.toString(Charsets.UTF_8.name())
    }

    suspend fun connectToPeer(ipAddress: String, port: Int): Socket? = withContext(Dispatchers.IO) {
        try {
            Timber.d("Connecting to $ipAddress:$port...")
            val socket = Socket()
            socket.connect(InetSocketAddress(ipAddress, port), TransferConstants.SOCKET_TIMEOUT_MS)
            socket.soTimeout = TransferConstants.SOCKET_TIMEOUT_MS
            Timber.d("✓ Connected to $ipAddress:$port")
            socket
        } catch (e: Exception) {
            Timber.e(e, "Connection failed to $ipAddress:$port")
            null
        }
    }

    suspend fun sendMessage(socket: Socket, message: TransferMessage) = withContext(Dispatchers.IO) {
        try {
            val jsonStr = serializeTransferMessage(message)
            Timber.d("Sending: ${message::class.simpleName}")
            val output = socket.getOutputStream()
            output.write("$jsonStr\n".toByteArray())
            output.flush()
        } catch (e: Exception) {
            Timber.e(e, "Failed to send message")
            throw e
        }
    }

    suspend fun sendChunkData(socket: Socket, transferId: String, chunkIndex: Int, data: ByteArray) = withContext(Dispatchers.IO) {
        try {
            val crc32 = calculateCRC32(data)
            val header = "CHUNK:$transferId:$chunkIndex:${data.size}:$crc32\n"
            val out = socket.getOutputStream()
            out.write(header.toByteArray(Charsets.UTF_8))
            out.write(data)
            out.flush() // VERY IMPORTANT: ensure TCP buffer is flushed to OS
            Timber.d("✓ Chunk $chunkIndex sent (${data.size} bytes) crc=$crc32")

            // Optional: read ACK from server (simple protocol)
            // val bufferedIn = socket.getInputStream().bufferedReader()
            // val ack = bufferedIn.readLine() // blocking read — only if server sends ack
            // Timber.d("ACK from server: $ack")

        } catch (e: Exception) {
            Timber.e(e, "Failed to send chunk $chunkIndex")
            throw e
        }
    }



    fun stopServer() {
        try {
            serverSocket?.close()
            Timber.d("Server stopped")
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop server")
        }
    }

    private fun parseTransferMessage(jsonStr: String): TransferMessage {
        // Try to detect message type from JSON using JsonElement (safer than contains checks)
        return try {
            val element = json.parseToJsonElement(jsonStr)
            val obj = element.jsonObject

            when {
                // FileOffer (detect by fields)
                "fileName" in obj || "totalChunks" in obj -> {
                    json.decodeFromString<TransferMessage.FileOffer>(jsonStr)
                }
                // FileAccept
                "accepted" in obj || "transferId" in obj && "accepted" in obj -> {
                    json.decodeFromString<TransferMessage.FileAccept>(jsonStr)
                }
                // ChunkData (expects dataBase64 field)
                "dataBase64" in obj && "chunkIndex" in obj -> {
                    json.decodeFromString<TransferMessage.ChunkData>(jsonStr)
                }
                // Handshake
                "deviceId" in obj && "deviceName" in obj -> {
                    json.decodeFromString<TransferMessage.Handshake>(jsonStr)
                }

                "transferComplete" in obj || "TransferComplete" in obj -> {
                    json.decodeFromString<TransferMessage.TransferComplete>(jsonStr)
                }

                else -> {
                    Timber.v("Ignoring unknown message type: ${jsonStr.take(120)}")
                    throw IllegalArgumentException("Unknown message format")
                }
            }
        } catch (e: Exception) {
            Timber.w("Parse error: ${e.message}")
            throw IllegalArgumentException("Unknown message type")
        }
    }

    private fun serializeTransferMessage(message: TransferMessage): String {
        return json.encodeToString(TransferMessage.serializer(), message)
    }

    private fun calculateCRC32(data: ByteArray): Long {
        val crc = java.util.zip.CRC32()
        crc.update(data)
        return crc.value
    }
}