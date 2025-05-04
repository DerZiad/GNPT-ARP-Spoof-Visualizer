package org.npt.models;

import javafx.scene.control.ContextMenu;

import java.util.List;

public class Target extends Device {

    public Target(String deviceName, List<IpAddress> ipAddresses, double x, double y, ContextMenu contextMenu) {
        super(deviceName, ipAddresses, x, y, contextMenu);
    }
}
