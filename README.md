<div align="center">📡 FluxShare</div>
<div align="center">Fast • Secure • Peer-to-Peer File Sharing on Local Wi-Fi</div>
<div align="center">
🚀 Direct P2P transfers | No Cloud | No Internet | AES-GCM Encryption | Resumable Transfers
</div>

<hr/>
📸 Screenshots
<div align="center">

  <table>
    <tr>
      <td align="center">
        <img src="https://raw.githubusercontent.com/Rajamohan006/FluxShare/master/app/src/screenshots/1000119589.jpg" width="130"/><br/>
        <sub>Home</sub>
      </td>

  <td align="center">
    <img src="https://raw.githubusercontent.com/Rajamohan006/FluxShare/master/app/src/screenshots/1000119593.jpg" width="130"/><br/>
    <sub>History</sub>
  </td>

  <td align="center">
    <img src="https://raw.githubusercontent.com/Rajamohan006/FluxShare/master/app/src/screenshots/1000119587.jpg" width="130"/><br/>
    <sub>Settings</sub>
  </td>

  <td align="center">
    <img src="https://raw.githubusercontent.com/Rajamohan006/FluxShare/master/app/src/screenshots/1000119582.jpg" width="130"/><br/>
    <sub>Active Transfers</sub>
  </td>

  <td align="center">
    <img src="https://raw.githubusercontent.com/Rajamohan006/FluxShare/master/app/src/screenshots/1000119591.jpg" width="130"/><br/>
    <sub>Discovery</sub>
  </td>
</tr>
  </table>

</div>

<hr/>

🎯 **Overview**

FluxShare is a high-performance, peer-to-peer file transfer app for Android that works entirely on your local Wi-Fi network.  
No internet. No external server. No cloud.  
Perfect for offline file sharing, large files, and secure transfers.

🔥 **Why FluxShare?**

- 🚫 **No Cloud Required** — Everything stays in your LAN  
- ⚡ **Blazing Fast** — Direct device-to-device TCP transfers  
- 🔄 **Resumable Transfers** — Auto-resume after interruptions  
- 🔒 **AES-GCM Encryption** — 256-bit optional security  
- 🛡️ **Integrity Checks** — SHA-256 + CRC32 per chunk  
- 📊 **Real-Time Progress** — Speed, ETA, chunk visualization  
- 🎨 **Modern UI** — Material You design + Dark Mode

<hr/>

✨ **Features**

### 🔍 **Automatic Discovery**
- UDP broadcast-based discovery  
- Finds peers instantly on the same network  
- Real-time online/offline updates  

### 📦 **Smart Transfer Engine**
- Chunk-based transfers (256 KB chunks)  
- Resumable and fault-tolerant  
- Sliding-window throughput optimization  
- Multiple simultaneous transfers  

### 🔐 **Security & Integrity**
- SHA-256 full-file verification  
- CRC32 per-chunk validation  
- AES-GCM 256-bit optional encryption  
- 100% local network — no external servers  

### 📱 **User Experience**
- Background transfers (Foreground service)  
- Live speed, progress, ETA  
- Chunk map visualization  
- Notifications & transfer history

---

🧩 **Architecture**

FluxShare follows a clean, modular, layered architecture:

**Presentation (UI) → Domain (Use Cases) → Data Layer (Network, DB)**


### 🎨 **Presentation Layer**
- Jetpack Compose (Declarative UI)  
- ViewModels + StateFlow  
- Navigation Component  

### 🧠 **Domain Layer**
- Use cases (SRP — Single Responsibility Principle)  
- Repository interfaces  
- Core models  

### 🗄️ **Data Layer**
- Room database (transfer & chunk state)  
- UDP peer discovery + TCP file transfers  
- AES-GCM encryption, SHA-256 hashing, CRC32 validation  

---
🎮 **Usage**

### 🚀 Initial Setup
- Install the app on both devices  
- Grant required permissions:  
  - **Location** (Wi-Fi peer discovery)  
  - **Storage**  
  - **Notifications**  
- Connect both devices to the same Wi-Fi network  

### 📤 Sending Files
- Open **FluxShare**  
- Go to **Discovery** tab  
- Select a device  
- Pick files  
- Track progress in **Transfers**  

### 📥 Receiving Files
- Auto-receive enabled  
- Files saved to:  
  **Downloads/FluxShare/**
- SHA-256 verification is performed automatically  

---

### 🔧 Configuration

**Default constants (`TransferConstants.kt`):**
````kotlin
object TransferConstants {
    const val DEFAULT_PORT = 8888
    const val DISCOVERY_PORT = 8889
    const val CHUNK_SIZE = 256 * 1024
    const val MAX_RETRIES = 3
    const val DISCOVERY_INTERVAL_MS = 5000L
}
````
---
### 🐛 Troubleshooting

**Device Not Detected**
- Ensure both devices are on the same Wi-Fi network
- Grant Location permission
- Disable VPN / Private DNS
- Ensure router allows UDP traffic on port 8889

**Transfer Failures**
- Ensure TCP port 8888 is open
- Exclude app from Battery Optimization
- Keep devices active and unlocked during transfer

**File Missing**
- Check: `Downloads/FluxShare/`
- Verify storage permission
- Ensure enough free space

---

### 📝 Changelog

**v1.0.0 – Initial Release**
- UDP peer discovery
- Chunked TCP transfer
- Resumable transfers
- SHA-256 + CRC32 verification
- Real-time progress updates
- Transfer history
- Optional AES-GCM encryption
- Material3 UI + Dark Mode

