package org.npt.models;

import javafx.scene.control.ContextMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SelfDevice extends Device {

    @Getter
    @Setter
    private List<Gateway> nextGateways;

    @Getter
    @Setter
    private String networkInterface;

    @Getter
    @Setter
    private List<IpAddress> ipAddresses;

    public SelfDevice(String deviceName, List<IpAddress> ipAddresses, double x, double y, ContextMenu contextMenu, List<Gateway> nextGateways) {
        super(deviceName, x, y, contextMenu);
        this.nextGateways = nextGateways;
        this.ipAddresses = ipAddresses;
    }
}
