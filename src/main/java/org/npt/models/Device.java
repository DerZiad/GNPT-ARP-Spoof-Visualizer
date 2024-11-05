package org.npt.models;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import lombok.Data;
import org.npt.beans.ArpSpoofStarter;
import static org.npt.configuration.Configuration.*;

@Data
public class Device {

    private final String name;
    private final String ipAddress;
    private final ContextMenu contextMenu;

    private Type type = Type.GATEWAY;
    private double x;
    private double y;
    private MenuItem startStopSniffing;

    public Device(String name, String ipAddress, double x, double y) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.x = x;
        this.y = y;

        // Initialize the context menu with options for each device
        this.contextMenu = new ContextMenu();

        startStopSniffing = new MenuItem("Start Sniffing");
        startStopSniffing.setOnAction(e -> spoof());

        MenuItem detailsItem = new MenuItem("View Details");
        detailsItem.setOnAction(e -> showDeviceDetails());

        MenuItem editIpItem = new MenuItem("Edit IP Address");
        editIpItem.setOnAction(e -> editIpAddress());

        MenuItem removeItem = new MenuItem("Remove Device");
        removeItem.setOnAction(e -> removeDevice());

        contextMenu.getItems().addAll(startStopSniffing, detailsItem, editIpItem, removeItem);
    }

    public Device(String name, String ipAddress, double x, double y,Type type){
        this(name,ipAddress,x,y);
        this.type = type;
    }



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

    private void showDeviceDetails() {
        System.out.println("Showing details for " + name + " (" + ipAddress + ")");
    }

    private void editIpAddress() {
        System.out.println("Editing IP address of " + name);
    }

    private void removeDevice() {
        System.out.println("Removing device " + name);
    }

}

