package org.npt.models;

import lombok.Getter;

import java.util.Optional;

public class Interface extends Device {

    @Getter
    private final String ip;

    @Getter
    private final Optional<Gateway> gatewayOptional;

    @Getter
    private final String netmask;

    public Interface(final String networkInterface, final String ip, final String netmask, final Optional<Gateway> gatewayOptional) {
        super(networkInterface);
        this.ip = ip;
        this.netmask = netmask;
        this.gatewayOptional = gatewayOptional;
    }
}
