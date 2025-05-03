package org.npt.services.impl;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.TcpPort;
import org.pcap4j.packet.namednumber.UdpPort;
import org.xbill.DNS.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class PacketSniffer {

    private String networkInterface = "eth0"; // Set the correct network interface name
    private String testServerIp; // Optional target IP if needed
    private PcapHandle handle;

    public PacketSniffer(String targetIp) {
        this.testServerIp = targetIp;
    }

    public void startSniffing() {
        try {
            // Display available interfaces for debugging
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            allDevs.forEach(dev -> System.out.println("Device: " + dev.getName() + " - " + dev.getDescription()));

            // Initialize the network device
            PcapNetworkInterface device = Pcaps.getDevByName(networkInterface);
            if (device == null) {
                System.out.println("No device found for interface: " + networkInterface);
                return;
            }

            int snapshotLength = 65536; // Capture all packets without truncation
            int timeout = 10;          // Capture timeout in milliseconds
            handle = device.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, timeout);

            System.out.println("Pcap handle open: " + handle.isOpen());

            // Remove filter to capture all packet types (TCP, UDP, ARP, etc.)
            handle.setFilter("ip src " + testServerIp, BpfProgram.BpfCompileMode.NONOPTIMIZE); // Empty filter captures all packets
            System.out.println("Starting packet capture for all packet types.");

            // Start packet capture in a separate thread to avoid blocking
            new Thread(this::capturePackets).start();

        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    private void capturePackets() {
        try {
            while (true) {
                Packet packet = handle.getNextPacket();
                if (packet != null) {
                    logPacketDetails(packet);
                } else {
                    System.out.println("No packet captured in this cycle.");
                }
            }
        } catch (NotOpenException e) {
            e.printStackTrace();
        }
    }

    private void logPacketDetails(Packet packet) {
        // Check if the packet is an Ethernet packet
        EthernetPacket ethPacket = packet.get(EthernetPacket.class);
        if (ethPacket != null) {
            System.out.println("Ethernet Frame Detected");

            // Check the Ethernet frame type to identify if it carries IPv4, IPv6, or ARP
            int etherType = ethPacket.getHeader().getType().value();

            // Check for IPv4 packets
            if (etherType == 0x0800) { // 0x0800 is the EtherType for IPv4
                System.out.println("IPv4 Packet Detected");

                // Parse the IP header to get the destination IP address
                IpV4Packet ipV4Packet = ethPacket.getPayload().get(IpV4Packet.class);
                if (ipV4Packet != null) {
                    String srcIp = ipV4Packet.getHeader().getSrcAddr().getHostAddress();
                    String dstIp = ipV4Packet.getHeader().getDstAddr().getHostAddress();
                    System.out.println("Source IP: " + srcIp + ", Destination IP: " + dstIp);
                    performDnsLookup(dstIp);
               }
            }
            // Check for IPv6 packets
            else if (etherType == 0x86DD) { // 0x86DD is the EtherType for IPv6
                System.out.println("IPv6 Packet Detected");

                IpV6Packet ipV6Packet = ethPacket.getPayload().get(IpV6Packet.class);
                if (ipV6Packet != null) {
                    String srcIp = ipV6Packet.getHeader().getSrcAddr().getHostAddress();
                    String dstIp = ipV6Packet.getHeader().getDstAddr().getHostAddress();
                    System.out.println("Source IP: " + srcIp + ", Destination IP: " + dstIp);
                }
            }
            // Check for ARP packets
            else if (etherType == 0x0806) { // 0x0806 is the EtherType for ARP
                System.out.println("ARP Packet Detected");

                ArpPacket arpPacket = ethPacket.getPayload().get(ArpPacket.class);
                if (arpPacket != null) {
                    String srcMac = arpPacket.getHeader().getSrcHardwareAddr().toString();
                    String srcIp = arpPacket.getHeader().getSrcProtocolAddr().getHostAddress();
                    String dstIp = arpPacket.getHeader().getDstProtocolAddr().getHostAddress();
                    System.out.println("ARP Packet - Source MAC: " + srcMac + ", Source IP: " + srcIp + ", Destination IP: " + dstIp);
                }
            } else {
                // Unknown EtherType - Convert raw data to ASCII
                System.out.println("Unrecognized EtherType: " + String.format("0x%04X", etherType));
                System.out.println("Attempting to display raw data in ASCII format:");
                String asciiData = hexToAscii(ethPacket.getPayload().getRawData());
                System.out.println("ASCII Data:\n" + asciiData);
            }
        } else {
//            // Log if packet is not an Ethernet frame
//            System.out.println("Unknown Packet Type - Raw data: " + packet);
//            // Unknown EtherType - Convert raw data to ASCII
//            System.out.println("Attempting to display raw data in ASCII format:");
//            String asciiData = hexToAscii(packet.getRawData());
//            System.out.println("ASCII Data:\n" + asciiData);
        }
    }

    // Utility method to convert byte array to ASCII string
    private String hexToAscii(byte[] rawData) {
        StringBuilder ascii = new StringBuilder();
        for (byte b : rawData) {
            // Only convert printable ASCII characters; use '.' for non-printable
            if (b >= 32 && b <= 126) {
                ascii.append((char) b);
            } else {
                ascii.append('.');
            }
        }
        return ascii.toString();
    }

    // Utility method to parse an IPv4 address from raw data
    private String parseIpAddress(byte[] data, int start) {
        return (data[start] & 0xFF) + "." + (data[start + 1] & 0xFF) + "." + (data[start + 2] & 0xFF) + "." + (data[start + 3] & 0xFF);
    }

    // Utility method to parse an IPv6 address from raw data
    private String parseIpv6Address(byte[] data, int start) {
        StringBuilder ip = new StringBuilder();
        for (int i = 0; i < 16; i += 2) {
            ip.append(String.format("%02X%02X", data[start + i], data[start + i + 1]));
            if (i < 14) ip.append(":");
        }
        return ip.toString();
    }


    // Method to check if the packet is a DNS packet
    private boolean isDnsPacket(Packet packet) {
        UdpPacket udpPacket = packet.get(UdpPacket.class);
        if (udpPacket != null && udpPacket.getHeader().getDstPort().equals(UdpPort.DOMAIN)) {
            return true;
        }

        TcpPacket tcpPacket = packet.get(TcpPacket.class);
        return tcpPacket != null && tcpPacket.getHeader().getDstPort().equals(TcpPort.DOMAIN);
    }

    // Method to print DNS details using dnsjava
    private void printDnsDetails(Packet packet) {
        try {
            Message dnsMessage = new Message(packet.getRawData());
            System.out.println("DNS Packet Details:\n" + dnsMessage);
        } catch (IOException e) {
            System.out.println("Failed to parse DNS packet.");
            e.printStackTrace();
        }
    }

    public void stopSniffing() {
        if (handle != null && handle.isOpen()) {
            handle.close();
            System.out.println("Packet capture stopped.");
        }
    }

    private void performDnsLookup(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            System.out.println("Hostname for IP " + ipAddress + ": " + inetAddress.getHostName());
        } catch (UnknownHostException e) {
            System.out.println("No hostname found for IP: " + ipAddress);
        }
    }
}