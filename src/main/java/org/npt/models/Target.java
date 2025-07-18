package org.npt.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
public class Target extends Device {

    @Getter
    @Setter
    private String ip;

    public Target(String deviceName, String ip) {
        super(deviceName);
        this.ip = ip;
    }
}
