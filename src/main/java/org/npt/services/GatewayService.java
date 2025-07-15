package org.npt.services;

import org.npt.exception.InvalidInputException;
import org.npt.models.Gateway;
import org.npt.models.Target;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public interface GatewayService {

    Gateway create(final String deviceName, final String deviceInterface, final String[] ipAddresses, final Target[] nextDevices) throws InvalidInputException;

    void remove(final Gateway gateway);

    Optional<Gateway> findByDeviceName(final String deviceName);

    Optional<Gateway> findByTarget(final Target target);

    HashMap<Integer, Gateway> findByNetworkInterface(final String networkInterface);

    HashMap<Integer, Gateway> findByIpAddress(final String ipAddress);

    Collection<Gateway> find();
}
