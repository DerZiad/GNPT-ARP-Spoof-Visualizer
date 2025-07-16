# 🧪 Graphical Network Packet Tracer (GNPT)

A JavaFX-based ARP spoofing tool with real-time packet capture and visualization.
Powered by **Pcap4J**, it allows you to analyze live network traffic during spoofing operations.

---

## 📦 Download

Latest release:
👉 [**Download v0.1.0 - binary\_file\_gnpt.zip**](https://github.com/DerZiad/GraphicalNetworkPacketTracer/releases/download/v0.1.0/binary_file_gnpt.zip)

---

## ✅ System Requirements

**🖥️ OS:**

* Linux (only)

**📦 Required Package:**

```bash
sudo apt-get install -y dsniff
```

**🔐 Permissions:**

* Must be run with `sudo` for low-level network access.

---

## 🚀 Running the App (Binary)

1. **Extract the zip file:**

   ```bash
   unzip binary_file_gnpt.zip
   cd gnpt
   ```

2. **Make the launcher executable:**

   ```bash
   chmod +x run.sh
   ```

3. **Start the application:**

   ```bash
   sudo ./run.sh
   ```

---

## 🧠 Features

* ⚡ ARP spoofing on local networks
* 📊 Real-time packet analysis and charts
* 🎛️ Graphical interface built with JavaFX
* 🧵 Efficient packet capture via **Pcap4J**
* 🔍 Automatically scans for device IPs on your network

---

## 🛠️ Building from Source

### 🔧 Prerequisites

* Java 21+
* Maven 3.6+
* Root/Administrator privileges
* Internet connection (Maven will auto-fetch JavaFX SDK)

### 📦 Build Instructions

```bash
git clone https://github.com/DerZiad/GraphicalNetworkPacketTracer.git
cd NetworkPacketTracer
mvn clean compile exec:java
```

---

## 🧯 Troubleshooting

| Issue               | Solution                                                 |
| ------------------- | -------------------------------------------------------- |
| `JavaFX errors`     | Make sure JavaFX modules are included in the module path |
| `Permission denied` | Run the app as `sudo`                                    |
| `dsniff not found`  | Install via `apt` as shown above                         |

---

## 🙏 Acknowledgments

* [Pcap4J](https://github.com/kaitoy/pcap4j) — packet capture library
* JavaFX & ControlsFX — UI and controls
* Apache Maven — dependency management and builds

---