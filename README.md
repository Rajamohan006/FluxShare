🎯 Overview
FluxShare is a modern, peer-to-peer file sharing application built natively for Android. Share files directly between devices on the same Wi-Fi network—no cloud, no server, no internet required. With resumable transfers, real-time progress tracking, and optional encryption, FluxShare makes large file transfers fast, reliable, and secure.
Why FluxShare?

🚫 No Cloud Required - Files stay on your local network
⚡ Blazing Fast - Direct device-to-device transfers over LAN
🔄 Resumable Transfers - Interrupted? Pick up where you left off
🔒 Privacy First - Optional AES-GCM encryption
✅ Integrity Checks - SHA-256 file verification + CRC32 per chunk
📊 Real-Time Progress - Live speed, ETA, and chunk visualization
🎨 Material3 Design - Beautiful, modern UI with dark mode


✨ Features
🔍 Automatic Discovery

UDP broadcast-based device discovery
Finds devices instantly on the same Wi-Fi network
Real-time device status updates

📦 Smart Transfer Engine

Chunked Transfers: 256KB chunks for reliability
Resumable: Restart interrupted transfers without losing progress
Concurrent: Multiple file transfers simultaneously
Adaptive: Sliding window optimization for maximum throughput

🔐 Security & Integrity

SHA-256 Verification: Guarantees file correctness
CRC32 Checksums: Per-chunk integrity validation
Optional AES-GCM Encryption: 256-bit end-to-end encryption
No Central Server: All data stays on your LAN

📱 User Experience

Foreground Service: Transfers continue in background
Live Progress: Real-time speed, ETA, and progress bars
Chunk Map Visualization: See exactly which chunks are complete
Smart Notifications: Progress updates and completion alerts
Transfer History: Track all past transfers with verification status


📸 Screenshots
<div align="center">

<img src="screenshots/1000119589.jpg" width="200"/> <img src="screenshots/1000119593.jpg" width="200"/> <img src="screenshots/1000119587.jpg" width="200"/> <img src="screenshots/1000119582.jpg" width="200"/>  <img src="screenshots/1000119591.jpg" width="200"/>
Home • Discovery • Active Transfers • History
</div>
Layer Responsibilities
Presentation Layer

Jetpack Compose UI: Modern declarative UI
ViewModels: State management with StateFlow
Navigation: Type-safe navigation component

Domain Layer

Use Cases: Single-responsibility business logic
Repositories: Data abstraction layer
Models: Core business entities

Data Layer

Room Database: Persistent transfer state & history
Network: UDP discovery + TCP chunked transfers
Security: AES-GCM encryption, SHA-256 hashing, CRC32


🛠️** Tech Stack**
Core Technologies
CategoryTechnologyPurposeLanguageKotlin 1.9.23Primary development languageUI FrameworkJetpack Compose 1.5Modern declarative UIDesign SystemMaterial Design 3UI components & themingArchitectureMVVM + Clean ArchitectureSeparation of concernsAsyncKotlin Coroutines + FlowConcurrency & reactive streams
Android Jetpack
ComponentVersionPurposeRoom2.6.1Local database (transfers, chunks)Hilt2.48Dependency injectionNavigation2.7.7Screen navigationLifecycle2.7.0ViewModel & lifecycle awarenessWorkManager2.9.0Background task scheduling
Networking & Security
LibraryPurposeJava Socket APITCP/UDP communicationKotlinx SerializationJSON serialization for messagesjavax.cryptoAES-GCM encryptionjava.securitySHA-256 hashingjava.util.zipCRC32 checksums
Development Tools
ToolPurposeAndroid Studio Hedgehog+IDEGradle 8.2Build systemKSP 1.9.23Kotlin Symbol ProcessingTimberLogging

🎮 **Usage**
Initial Setup

Install on Both Devices: FluxShare needs to be installed on both sender and receiver
Grant Permissions: Allow Location (for Wi-Fi discovery), Storage, and Notifications
Connect to Same Wi-Fi: Both devices must be on the same network

Sending Files

Open FluxShare on both devices
Go to Discovery tab
Wait for devices to appear
Tap on target device
Select file(s) to send
Monitor progress in Transfers tab

Receiving Files
Files are automatically received in the background!

Location: Downloads/FluxShare/
Notifications: You'll see progress and completion alerts
Verification: SHA-256 hash verified automatically

Transfer History
View all past transfers in the History tab:

✅ Completed transfers with verification status
❌ Failed transfers with error details
📊 Transfer statistics (size, speed, time)


🔧 **Configuration**
Default Settings
SettingDefault ValuePurposeTCP Port8888File transfer connectionUDP Port8889Device discovery broadcastsChunk Size256 KBTransfer chunk sizeMax Retries3Chunk retry attemptsDiscovery Interval5 secondsBroadcast frequencyPeer Timeout30 secondsDevice offline threshold
Customization
Modify constants in TransferConstants.kt:
kotlinobject TransferConstants {
    const val DEFAULT_PORT = 8888
    const val DISCOVERY_PORT = 8889
    const val CHUNK_SIZE = 256 * 1024 // 256KB
    const val MAX_RETRIES = 3
    const val DISCOVERY_INTERVAL_MS = 5000L
}

🔐 Security Features
File Integrity

SHA-256 Hashing: Full file verification

kotlin   val hash = securityManager.calculateFileHash(filePath)
   val verified = securityManager.verifyFileIntegrity(filePath, expectedHash)

CRC32 Checksums: Per-chunk validation

kotlin   val crc = calculateCRC32(chunkData)
   val valid = verifyCRC32(chunkData, expectedCRC)
Optional Encryption
Enable in Settings for AES-GCM 256-bit encryption:
kotlinval key = securityManager.generateEncryptionKey()
val encrypted = securityManager.encryptData(data, key)
val decrypted = securityManager.decryptData(encrypted, key)
Network Security

Local Network Only: All transfers stay on LAN
No Central Server: Direct P2P connections
No Cloud Upload: Files never leave your network
Clear Protocol: Simple, auditable message format


🧪 **Testing**
Run Unit Tests
bash./gradlew test
Run Instrumented Tests
bash./gradlew connectedAndroidTest
Manual Testing
Two-Device Test:

Install on Device A and B
Connect both to same Wi-Fi
Open app on both devices
Device A: Send a file to Device B
Device B: Check Downloads/FluxShare/
Verify file received correctly


🐛** Troubleshooting**
Discovery Issues
Problem: Devices not appearing
Solutions:

✅ Check both devices on same Wi-Fi network
✅ Grant Location permission (required for Wi-Fi scanning)
✅ Disable VPN on both devices
✅ Check router allows UDP broadcast (port 8889)

Transfer Failures
Problem: Connection failed
Solutions:

✅ Ensure TCP port 8888 not blocked by firewall
✅ Check Wi-Fi signal strength
✅ Disable battery optimization for FluxShare
✅ Keep both devices unlocked during transfer

File Not Received
Problem: Transfer shows complete but file missing
Solutions:

✅ Check Downloads/FluxShare/ directory
✅ Grant Storage permission
✅ Ensure enough storage space
✅ Check logs: adb logcat | grep FluxShare


📝** Changelog**
Version 1.0.0 (Initial Release)

✨ Automatic UDP device discovery
✨ Chunked TCP file transfers
✨ Resumable transfer support
✨ SHA-256 file verification
✨ CRC32 per-chunk checksums
✨ Real-time progress tracking
✨ Transfer history
✨ Material3 design with dark mode
✨ Optional AES-GCM encryption
