package org.npt.models;

import javafx.scene.control.ContextMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Target extends Device {

    @Getter
    @Setter
    private String networkInterface;

    @Getter
    @Setter
    private List<String> ipAddresses;

    public Target(String deviceName, String networkInterface, List<String> ipAddresses, double x, double y, ContextMenu contextMenu) {
        super(deviceName, x, y, contextMenu);
        this.ipAddresses = ipAddresses;
        this.networkInterface = networkInterface;
    }
}
