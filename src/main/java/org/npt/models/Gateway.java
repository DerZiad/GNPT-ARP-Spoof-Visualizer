package org.npt.models;

import javafx.scene.control.ContextMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Gateway extends Device {

    @Getter
    @Setter
    private List<Device> devices;

    public Gateway(String deviceName, List<IpAddress> ipAddresses, double x, double y, ContextMenu contextMenu, List<Device> devices) {
        super(deviceName, ipAddresses, x, y, contextMenu);
        this.devices = devices;
    }
}
