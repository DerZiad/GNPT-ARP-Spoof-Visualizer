package org.npt.models;

import lombok.Getter;

public class Interface extends Device {

    @Getter
    private final String ip;

    @Getter
    private final Gateway gateway;

    public Interface(String networkInterface, String ip, Gateway gateway) {
        super(networkInterface);
        this.ip = ip;
        this.gateway = gateway;
    }
}
