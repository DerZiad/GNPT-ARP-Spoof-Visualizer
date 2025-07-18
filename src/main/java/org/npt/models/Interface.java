package org.npt.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public class Interface extends Device {

    @Getter
    @Setter
    private String ip;

    @Getter
    @Setter
    private Optional<Gateway> gatewayOptional;

    @Getter
    @Setter
    private String netmask;

    public Interface(final String networkInterface, final String ip, final String netmask, final Optional<Gateway> gatewayOptional) {
        super(networkInterface);
        this.ip = ip;
        this.netmask = netmask;
        this.gatewayOptional = gatewayOptional;
    }
}
