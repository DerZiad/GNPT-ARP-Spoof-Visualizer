package org.npt;

import org.npt.beans.ArpSpoofStarter;
import org.npt.beans.GatewayFinder;
import org.npt.beans.LogStorageCreator;
import org.npt.beans.PacketSniffer;
import org.npt.configuration.Configuration;
import org.npt.exception.GatewayNotFoundException;
import org.npt.exception.ProcessFailureException;
import org.npt.models.Device;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    /*public static void main(String[] args) throws ProcessFailureEeption, GatewayNotFoundException, IOException {
        String logStorageFolder = LogStorageCreator.createLogStorageFolder();
        GatewayFinder gatewayFinder = GatewayFinder.getInstance();
        List<Device> targets = Arrays.asList("192.168.178.32").stream().map((String ip) -> new Device(ip, ip,0,0)).toList();
        Device gateway = gatewayFinder.getGateway();
        Configuration.gateway = gatewayFinder.getGateway();
        Configuration.scanInterface = "eth0";
        ArpSpoofStarter arpspoofStarter = new ArpSpoofStarter();
        targets.forEach(arpspoofStarter::addDevice);
        arpspoofStarter.startSpoofing(targets);
        PacketSniffer packetSniffer = new PacketSniffer("192.168.178.32");
        packetSniffer.startSniffing();
    }*/
}
