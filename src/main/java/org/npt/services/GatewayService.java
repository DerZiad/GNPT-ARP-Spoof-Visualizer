package org.npt.services;

import org.npt.exception.InvalidInputException;
import org.npt.exception.NotFoundException;
import org.npt.models.Gateway;

import java.util.List;
import java.util.Optional;

public interface GatewayService {

    public Gateway create(String deviceName, String deviceInterface, String[] ipAddresses) throws InvalidInputException;

    public void remove(Gateway gateway) throws NotFoundException;

    public Optional<Gateway> findByDeviceName(String deviceName);

    public List<Gateway> findByNetworkInterface(String networkInterface);

    public List<Gateway> findByIpAddress(String ipAddress);
}
