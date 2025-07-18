package org.npt.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class SelfDevice extends Device {

    @Getter
    @Setter
    private List<Interface> anInterfaces;

    public SelfDevice(String deviceName, List<Interface> anInterfaces) {
        super(deviceName);
        this.anInterfaces = anInterfaces;
    }
}
