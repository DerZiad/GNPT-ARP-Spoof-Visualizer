package org.npt.models;

import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

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

    public SelfDevice(String deviceName, List<IpAddress> ipAddresses, List<Gateway> nextGateways) {
        super(deviceName);
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
