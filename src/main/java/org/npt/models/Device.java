package org.npt.models;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.npt.beans.implementation.ArpSpoofStarter;
import static org.npt.configuration.Configuration.*;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class Device {

    private String deviceName;
    private String ipAddress;
    private double x;
    private double y;
    private Type type;
    private ContextMenu contextMenu;

    private void spoof(){
        ArpSpoofStarter arpSpoofStarter = new ArpSpoofStarter();
        arpSpoofStarter.startSpoofing(this);
        Device attacker = devices.stream().filter(device -> device.getType().equals(Type.SELF)).findFirst().get();
        Connection connectionz = connections.stream().filter((Connection connection)->connection.getFirstDevice().equals(this) || connection.getSecondDevice().equals(this)).findFirst().get();
        if(connectionz.getFirstDevice().equals(this)){
            connectionz.setSecondDevice(attacker);
        }else{
            connectionz.setFirstDevice(attacker);
        }

    }

    private void stopSpoofing(){
        ArpSpoofStarter arpSpoofStarter = new ArpSpoofStarter();
        arpSpoofStarter.stopSpoofing(this);
        Connection connection = connections.stream().filter((Connection c)->c.getFirstDevice().equals(this) || c.getSecondDevice().equals(this)).findFirst().get();
        if(connection.getFirstDevice().equals(this)){
            connection.setSecondDevice(gateway);
        }else{
            connection.setFirstDevice(gateway);
        }
    }

    private void initMenu(){
        MenuItem detailsItem = new MenuItem("View Details");
        detailsItem.setOnAction(e -> showDeviceDetails());

        MenuItem editIpItem = new MenuItem("Edit IP Address");
        editIpItem.setOnAction(e -> editIpAddress());

        MenuItem removeItem = new MenuItem("Remove Device");
        removeItem.setOnAction(e -> removeDevice());

        MenuItem startStopSniffing = new MenuItem("Start Sniffing");
        startStopSniffing.setOnAction(e -> spoof());

        contextMenu.getItems().addAll(startStopSniffing, detailsItem, editIpItem, removeItem);
    }

    private void showDeviceDetails() {
        System.out.println("Showing details for " + deviceName + " (" + ipAddress + ")");
    }

    private void editIpAddress() {
        System.out.println("Editing IP address of " + deviceName);
    }

    private void removeDevice() {
        System.out.println("Removing device " + deviceName);
    }

}

