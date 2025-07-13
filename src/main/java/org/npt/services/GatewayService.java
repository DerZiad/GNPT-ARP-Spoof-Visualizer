package org.npt.services;

import org.npt.exception.InvalidInputException;
import org.npt.models.Gateway;
import org.npt.models.Target;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public interface GatewayService {

    public Gateway create(final String deviceName, final String deviceInterface, final String[] ipAddresses, final Target[] nextDevices) throws InvalidInputException;

    public void remove(final Gateway gateway);

    public Optional<Gateway> findByDeviceName(final String deviceName);

    public HashMap<Integer, Gateway> findByNetworkInterface(final String networkInterface);

    public HashMap<Integer, Gateway> findByIpAddress(final String ipAddress);

    public Collection<Gateway> find();
}
