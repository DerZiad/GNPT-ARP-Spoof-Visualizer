package org.npt.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

public class Target extends Device {

    @Getter
    @Setter
    private String networkInterface;

    @Getter
    @Setter
    private List<String> ipAddresses;

    public Target(String deviceName, String networkInterface, List<String> ipAddresses) {
        super(deviceName);
        this.ipAddresses = ipAddresses;
        this.networkInterface = networkInterface;
    }

    public Optional<String> findFirstIPv4() {
        for (String ip : this.ipAddresses) {
            if (isValidIPv4(ip)) {
                return Optional.ofNullable(ip);
            }
        }
        return Optional.empty();
    }
}
