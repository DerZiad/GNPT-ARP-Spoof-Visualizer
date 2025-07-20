package org.npt.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public final class SelfDevice extends Device {

    private List<Interface> anInterfaces = new CopyOnWriteArrayList<>();

    public SelfDevice(String deviceName) {
        super(deviceName);
    }

    public void addInterface(Interface anInterface) {
        if (anInterfaces == null) {
            anInterfaces = new ArrayList<>();
        }
        anInterfaces.add(anInterface);
    }

    public void addInterfaces(List<Interface> interfaces) {
        if (anInterfaces == null) {
            anInterfaces = new ArrayList<>();
        }
        anInterfaces.addAll(interfaces);
    }

    public Optional<Interface> getInterfaceIfExist(String displayName) {
        return anInterfaces.stream()
                .filter(anInterface -> Objects.equals(anInterface.getDeviceName(), displayName))
                .findFirst();
    }
}
