package org.npt.models;

import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

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

    public Gateway(String deviceName, String networkInterface, List<String> ipAddresses, List<Target> devices) {
        super(deviceName);
        this.devices = devices;
        this.networkInterface = networkInterface;
        this.ipAddresses = ipAddresses;
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
