package org.npt.models;

import javafx.scene.control.ContextMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

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

    public Optional<IpAddress> findFirstIPv4() {
        for (IpAddress ipObject : this.ipAddresses) {
            if (isValidIPv4(ipObject.getIp())) {
                return Optional.of(ipObject);
            }
        }
        return Optional.empty();
    }
}
