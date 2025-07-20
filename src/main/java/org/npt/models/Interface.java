package org.npt.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public final class Interface extends Device {

    private String ip;

    private String netmask;

    private Gateway gateway;

    public Interface(final String networkInterface, final String ip, final String netmask, final Gateway gateway) {
        super(networkInterface);
        this.ip = ip;
        this.netmask = netmask;
        this.gateway = gateway;
    }

    public boolean targetAlreadyScanned(Target target) {
        if (gateway != null && gateway.getDevices() != null) {
            return gateway.getDevices().contains(target);
        }
        return false;
    }

    public boolean targetAlreadyScanned(String ip) {
        if (gateway != null && gateway.getDevices() != null) {
            return gateway.getDevices().stream()
                    .anyMatch(target -> Objects.equals(target.getIp(), ip));
        }
        return false;
    }
}
