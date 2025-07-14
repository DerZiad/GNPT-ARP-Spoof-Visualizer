package org.npt.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import org.npt.exception.InvalidInputException;
import org.npt.models.Target;

public interface TargetService {

    Target create(final String deviceName, final String deviceInterface, final String[] ipAddresses) throws InvalidInputException;

    void remove(final Target target);

    Optional<Target> findByDeviceName(final String deviceName);

    HashMap<Integer, Target> findByNetworkInterface(final String networkInterface);

    HashMap<Integer, Target> findByIpAddress(final String ipAddress);

    Collection<Target> find();

}
