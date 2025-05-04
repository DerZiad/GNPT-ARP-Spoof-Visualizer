package org.npt.services.impl;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.npt.models.Device;

public class PicassoService {



    private void spoof(Device arg){
        /*ArpSpoofStarter arpSpoofStarter = new ArpSpoofStarter();
        arpSpoofStarter.startSpoofing(arg);
        Device attacker = devices.stream().filter(device -> device.getType().equals(Type.SELF)).findFirst().get();
        Connection connectionz = connections.stream().filter((Connection connection)->connection.getFirstDevice().equals(this) || connection.getSecondDevice().equals(this)).findFirst().get();
        if(connectionz.getFirstDevice().equals(this)){
            connectionz.setSecondDevice(attacker);
        }else{
            connectionz.setFirstDevice(attacker);
        }*/

    }

    private void stopSpoofing(Device device){
        /*ArpSpoofStarter arpSpoofStarter = new ArpSpoofStarter();
        arpSpoofStarter.stopSpoofing(device);
        Connection connection = connections.stream().filter((Connection c)->c.getFirstDevice().equals(this) || c.getSecondDevice().equals(this)).findFirst().get();
        if(connection.getFirstDevice().equals(this)){
            connection.setSecondDevice(gateway);
        }else{
            connection.setFirstDevice(gateway);
        }*/
    }

    public void initMenu(Device device){
        ContextMenu contextMenu = device.getContextMenu();
        MenuItem detailsItem = new MenuItem("View Details");
        detailsItem.setOnAction(e -> showDeviceDetails(device));

        MenuItem editIpItem = new MenuItem("Edit IP Address");
        editIpItem.setOnAction(e -> editIpAddress(device));

        MenuItem removeItem = new MenuItem("Remove Device");
        removeItem.setOnAction(e -> removeDevice(device));

        MenuItem startStopSniffing = new MenuItem("Start Sniffing");
        startStopSniffing.setOnAction(e -> spoof(device));

        contextMenu.getItems().addAll(startStopSniffing, detailsItem, editIpItem, removeItem);
    }

    private void showDeviceDetails(Device device) {
        //System.out.println("Showing details for " + device.getDeviceName() + " (" + device.getIpAddress() + ")");
    }

    private void editIpAddress(Device device) {
        System.out.println("Editing IP address of " + device.getDeviceName());
    }

    private void removeDevice(Device device) {
        System.out.println("Removing device " + device.getDeviceName());
    }

}
