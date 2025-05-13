package org.npt.networkservices;

import lombok.Getter;
import org.npt.models.DefaultPacket;
import org.npt.models.Target;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

class DefaultPacketSniffer implements PacketSniffer {

    @Getter
    private final Target target;

    private final String networkInterface;
    private PcapHandle handle;

    @Getter
    private final List<DefaultPacket> defaultPackets = new ArrayList<>(4000);
    private boolean running = true;

    private DefaultPacketSniffer(Target target, String networkInterface) {
        this.target = target;
        this.networkInterface = networkInterface;
    }

    public static DefaultPacketSniffer create(Target target, String networkInterface) {
        return new DefaultPacketSniffer(target, networkInterface);
    }

    public void stop() {
        running = false;
        stopSniffing();
    }

    @Override
    public void run() {
        startSniffing();
        try {
            while (running) {
                Packet packet = handle.getNextPacket();
                if (packet != null) {
                    processPacket(packet);
                }
            }
        } catch (NotOpenException e) {
            e.printStackTrace();
        }
    }

    private void processPacket(Packet packet) {
        String srcIp = null;
        String dstIp = null;
        String type = "UNKNOWN";

        // Handle ARP
        ArpPacket arp = packet.get(ArpPacket.class);
        if (arp != null) {
            srcIp = arp.getHeader().getSrcProtocolAddr().getHostAddress();
            dstIp = arp.getHeader().getDstProtocolAddr().getHostAddress();
            type = "ARP";
        }

        // Handle IPv4 or IPv6
        IpV4Packet ipv4 = packet.get(IpV4Packet.class);
        if (ipv4 != null) {
            srcIp = ipv4.getHeader().getSrcAddr().getHostAddress();
            dstIp = ipv4.getHeader().getDstAddr().getHostAddress();
            Packet payload = ipv4.getPayload();
            type = getPayloadType(payload, dstIp);
        }

        IpV6Packet ipv6 = packet.get(IpV6Packet.class);
        if (ipv6 != null) {
            srcIp = ipv6.getHeader().getSrcAddr().getHostAddress();
            dstIp = ipv6.getHeader().getDstAddr().getHostAddress();
            Packet payload = ipv6.getPayload();
            type = getPayloadType(payload, dstIp);
        }

        // Fallback to Ethernet
        if (srcIp == null || dstIp == null) {
            EthernetPacket eth = packet.get(EthernetPacket.class);
            if (eth != null && eth.getPayload() != null) {
                processPacket(eth.getPayload());
                return;
            }
        }

        if (srcIp == null || dstIp == null) return;

        // Reverse DNS lookup (best effort)
        String resolvedSrc = resolveHostname(srcIp);
        String resolvedDst = resolveHostname(dstIp);

        DefaultPacket defaultPacket = DefaultPacket.builder()
                .srcIp(resolvedSrc)
                .dstIp(resolvedDst)
                .type(type)
                .build();

        defaultPackets.add(defaultPacket);
    }

    private String getPayloadType(Packet payload, String dstIp) {
        switch (payload) {
            case null -> {
                return "UNKNOWN";
            }
            case TcpPacket tcp -> {
                int port = tcp.getHeader().getDstPort().valueAsInt();
                if (port == 22) return "SSH";
                if (port == 53) {
                    System.out.println("DNS Request Detected to " + dstIp);
                    return "DNS";
                }
                return "TCP";
            }
            case UdpPacket udp -> {
                int port = udp.getHeader().getDstPort().valueAsInt();
                if (port == 53) {
                    System.out.println("DNS Request Detected to " + dstIp);
                    return "DNS";
                }
                return "UDP";
            }
            case IcmpV4CommonPacket packets -> {
                return "ICMPv4";
            }
            case IcmpV6CommonPacket packets -> {
                return "ICMPv6";
            }
            default -> {
            }
        }

        return payload.getClass().getSimpleName();
    }

    private String resolveHostname(String ip) {
        try {
            InetAddress inet = InetAddress.getByName(ip);
            return inet.getHostName();
        } catch (UnknownHostException e) {
            return ip; // fallback to IP if DNS fails
        }
    }

    public void startSniffing() {
        try {
            PcapNetworkInterface device = Pcaps.getDevByName(networkInterface);
            if (device == null) {
                System.out.println("No device found for interface: " + networkInterface);
                return;
            }

            handle = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
            String filter = "ip src " + target.getIpAddresses().getFirst();
            handle.setFilter(filter, BpfProgram.BpfCompileMode.NONOPTIMIZE);

            System.out.println("Packet capture started on: " + networkInterface);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    public void stopSniffing() {
        if (handle != null && handle.isOpen()) {
            handle.close();
            System.out.println("Packet capture stopped.");
        }
    }
}

public interface PacketSniffer extends Runnable {

    public void startSniffing();

    public void stopSniffing();

    public void stop();

    public Target getTarget();

    public List<DefaultPacket> getDefaultPackets();

    public static PacketSniffer create(Target target, String networkInterface) {
        return DefaultPacketSniffer.create(target, networkInterface);
    }

}
