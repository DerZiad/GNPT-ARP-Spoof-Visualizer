package org.npt.services;

import org.npt.exception.InvalidInputException;
import org.npt.exception.NotFoundException;
import org.npt.models.Target;

import java.util.List;
import java.util.Optional;

public interface TargetService {

    public Target create(String deviceName, String deviceInterface, String[] ipAddresses) throws InvalidInputException;

    public void remove(Target target) throws NotFoundException;

    public Optional<Target> findByDeviceName(String deviceName);

    public List<Target> findByNetworkInterface(String networkInterface);

    public List<Target> findByIpAddress(String ipAddress);

}
