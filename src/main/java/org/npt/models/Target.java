package org.npt.models;

import javafx.scene.control.ContextMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Target extends Device {

    @Getter
    @Setter
    private Device next;

    public Target(String deviceName, List<IpAddress> ipAddresses, double x, double y, ContextMenu contextMenu, Device next) {
        super(deviceName, ipAddresses, x, y, contextMenu);
        this.next = next;
    }
}
