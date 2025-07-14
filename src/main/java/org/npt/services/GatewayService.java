package org.npt.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import org.npt.exception.InvalidInputException;
import org.npt.models.Gateway;
import org.npt.models.Target;

public interface GatewayService {

    public Gateway create(final String deviceName, final String deviceInterface, final String[] ipAddresses, final Target[] nextDevices) throws InvalidInputException;

    public void remove(final Gateway gateway);

    public Optional<Gateway> findByDeviceName(final String deviceName);

    Optional<Gateway> findByTarget(final Target target);

    public HashMap<Integer, Gateway> findByNetworkInterface(final String networkInterface);

    public HashMap<Integer, Gateway> findByIpAddress(final String ipAddress);

    public Collection<Gateway> find();
}
