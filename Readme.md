# NetworkPacketTracer

## Overview

**NetworkPacketTracer** is a Java-based ARP spoofing project that captures and analyzes network packets in real-time. The captured packets are processed and displayed statistically via a JavaFX-based user interface, providing insights into network traffic during the spoofing operation.

---

## Features

* Perform ARP spoofing on the local network.
* Capture network packets using the Pcap4J library.
* Display detailed statistics of sniffed packets in real-time.
* Rich JavaFX UI with charts and controls for intuitive monitoring.

---

## Prerequisites

* **Java Development Kit (JDK) 21** or later installed.
* **Maven 3.6+** installed.
* **Root or Administrator privileges** (required for packet sniffing).
* JavaFX SDK downloaded (the project includes an automated task for this).
* Network interface with permissions to sniff packets.

---

## Setup & Build Instructions

1. **Clone the repository** (if applicable):

```bash
git clone https://your-repo-url.git
cd NetworkPacketTracer
```

2. **Ensure JavaFX SDK is available**:

The Maven build includes a step to download and extract the JavaFX SDK automatically. This happens during the `generate-resources` phase. You can manually trigger it by running:

```bash
mvn generate-resources
```

3. **Build the project**:

Compile and package the application along with all dependencies:

```bash
mvn clean package
```

This will generate a JAR file `NetworkPacketTracer-1.0.jar` with all dependencies included in the `target` directory.

---

## Running the Application

Because sniffing packets requires elevated privileges, you need to run the application with `sudo` (Linux/macOS) or as Administrator (Windows).

### Run with Maven:

From your project root directory:

```bash
sudo mvn clean compile exec:java
```

* This cleans, compiles, and runs the main JavaFX application.
* You will be prompted to enter your password for `sudo`.

### Run the packaged JAR:

Alternatively, after packaging, you can run the JAR directly:

```bash
sudo java --module-path ./javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.swing -jar target/NetworkPacketTracer-1.0.jar
```

Make sure the `javafx/lib` directory exists and contains the JavaFX SDK libraries as expected.

---

## Configuration

* The `pom.xml` contains all required dependencies and JavaFX configuration.
* Modify the `java.fx.lib.path` property in the `pom.xml` if your JavaFX SDK path differs.
* The main class is set as `org.npt.Launch`.

---

## Troubleshooting

* **JavaFX errors**: Verify that the JavaFX SDK is correctly downloaded and the module path is set properly.
* **Permission issues**: Ensure you run the application with sufficient privileges (`sudo` on Linux/macOS).
* **Maven errors**: Check Maven installation and network connectivity for downloading dependencies.

---

## Acknowledgments

* [Pcap4J](https://github.com/kaitoy/pcap4j) for packet capturing.
* JavaFX community and ControlsFX for UI components.
* Maven for build automation.

---

