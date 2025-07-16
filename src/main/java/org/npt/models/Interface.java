package org.npt.models;

import lombok.Getter;

import java.util.Optional;

public class Interface extends Device {

    @Getter
    private final String ip;

    @Getter
    private final Optional<Gateway> gatewayOptional;

    public Interface(String networkInterface, String ip, Optional<Gateway> gatewayOptional) {
        super(networkInterface);
        this.ip = ip;
        this.gatewayOptional = gatewayOptional;
    }
}
