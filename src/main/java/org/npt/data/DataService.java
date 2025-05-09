package org.npt.data;

import org.npt.configuration.OnInit;
import org.npt.models.Device;
import org.npt.models.SelfDevice;
import org.npt.models.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface DataService extends OnInit {

    public List<Device> getDevices();

    public void addDevice(final Optional<Device> device) throws NullPointerException;

    public void removeByIndex(final Optional<Integer> index) throws NullPointerException;

    public void removeByObject(final Optional<Device> device) throws NullPointerException;

    public Optional<Device> getDevice(final Optional<Integer> index) throws NullPointerException;

    public <T> HashMap<Integer, T> getDevices(final Optional<Class<T>> tClass) throws NullPointerException;

    public SelfDevice getSelfDevice();

}
