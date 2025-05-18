package org.npt.networkservices;

import lombok.Getter;
import org.npt.models.DefaultPacket;
import org.npt.models.Target;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DefaultPacketSniffer implements PacketSniffer {

    @Getter
    private final Target target;

    private final String networkInterface;
    private PcapHandle handle;

    @Getter
    private final List<DefaultPacket> defaultPackets = Collections.synchronizedList(new ArrayList<>(4000));

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

        DefaultPacket defaultPacket = DefaultPacket.builder()
                .srcIp(srcIp)
                .dstIp(dstIp)
                .type(type)
                .build();

        System.out.println(srcIp + " -----> " + dstIp);
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
                    return "DNS";
                }
                return "TCP";
            }
            case UdpPacket udp -> {
                int port = udp.getHeader().getDstPort().valueAsInt();
                if (port == 53) {
                    return "DNS";
                }
                return "UDP";
            }
            case IcmpV4CommonPacket _ -> {
                return "ICMPv4";
            }
            case IcmpV6CommonPacket _ -> {
                return "ICMPv6";
            }
            default -> {
            }
        }

        return payload.getClass().getSimpleName();
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
