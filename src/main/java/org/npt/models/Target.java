package org.npt.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public final class Target extends Device {

    private String ip;

    public Target(String deviceName, String ip) {
        super(deviceName);
        this.ip = ip;
    }
}
