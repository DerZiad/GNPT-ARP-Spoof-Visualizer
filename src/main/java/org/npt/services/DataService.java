package org.npt.services;

import org.npt.models.Device;
import org.npt.models.SelfDevice;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface DataService {

    public List<Device> getDevices();

    public void addDevice(final Optional<Device> device) throws NullPointerException;

    public void removeByIndex(final Optional<Integer> index) throws NullPointerException;

    public void removeByObject(final Optional<Device> device) throws NullPointerException;

    public Optional<Device> getDevice(final Optional<Integer> index) throws NullPointerException;

    public <T> HashMap<Integer, T> getDevices(final Optional<Class<T>> tClass) throws NullPointerException;

    public SelfDevice getSelfDevice();

}
