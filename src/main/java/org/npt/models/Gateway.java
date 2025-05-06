package org.npt.models;

import javafx.scene.control.ContextMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Gateway extends Device {

    @Getter
    @Setter
    private List<Target> devices;
    @Getter
    @Setter
    private String networkInterface;
    @Getter
    @Setter
    private List<String> ipAddresses;

    public Gateway(String deviceName, String networkInterface, List<String> ipAddresses, double x, double y, ContextMenu contextMenu, List<Target> devices) {
        super(deviceName, x, y, contextMenu);
        this.devices = devices;
        this.networkInterface = networkInterface;
        this.ipAddresses = ipAddresses;
    }
}
