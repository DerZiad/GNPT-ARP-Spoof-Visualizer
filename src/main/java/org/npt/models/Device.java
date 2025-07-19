package org.npt.models;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@Data
public abstract sealed class Device implements Comparable<Device> permits Target, SelfDevice, Gateway, Interface {

    private String deviceName;
    private String key;
    private Double x = null;
    private Double y = null;

    protected Device(String deviceName) {
        this.deviceName = deviceName;
        this.key = UUID.randomUUID().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(deviceName, device.deviceName) && Objects.equals(key, device.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceName, key);
    }

    public Boolean isClose(Device device, double radius) {
        if (device == null) {
            return false;
        }
        double dx = this.x - device.getX();
        double dy = this.y - device.getY();
        return Math.sqrt(dx * dx + dy * dy) <= radius;
    }

    @Override
    public int compareTo(@NotNull Device o) {
        return 0;
    }

    public boolean initialized() {
        return this.x != null && this.y != null;
    }
}


