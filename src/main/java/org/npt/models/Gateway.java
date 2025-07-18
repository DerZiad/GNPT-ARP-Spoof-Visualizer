package org.npt.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class Gateway extends Device {

    @Getter
    @Setter
    private String ip;

    @Getter
    @Setter
    private List<Target> devices;

    public Gateway(String deviceName, String ip, List<Target> devices) {
        super(deviceName);
        this.devices = devices;
        this.ip = ip;
    }
}
