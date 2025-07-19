package org.npt.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public final class Gateway extends Device {

    private String ip;

    private List<Target> devices = new CopyOnWriteArrayList<>();

    public Gateway(String deviceName, String ip) {
        super(deviceName);
        this.devices = devices;
        this.ip = ip;
    }
}
