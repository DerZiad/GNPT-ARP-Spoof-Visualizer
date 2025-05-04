package org.npt.models;

import javafx.scene.control.ContextMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Gateway extends Device {

    @Getter
    @Setter
    private List<Target> devices;

    public Gateway(String deviceName, List<IpAddress> ipAddresses, double x, double y, ContextMenu contextMenu, List<Target> devices) {
        super(deviceName, ipAddresses, x, y, contextMenu);
        this.devices = devices;
    }
}
