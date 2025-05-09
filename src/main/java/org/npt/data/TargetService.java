package org.npt.data;

import org.npt.exception.InvalidInputException;
import org.npt.models.Target;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public interface TargetService {

    public Target create(final String deviceName, final String deviceInterface, final String[] ipAddresses) throws InvalidInputException;

    public void remove(final Target target);

    public Optional<Target> findByDeviceName(final String deviceName);

    public HashMap<Integer, Target> findByNetworkInterface(final String networkInterface);

    public HashMap<Integer, Target> findByIpAddress(final String ipAddress);

    public Collection<Target> find();

}
