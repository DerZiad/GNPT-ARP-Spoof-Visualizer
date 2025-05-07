package org.npt.services.impl;


import javafx.scene.control.ContextMenu;
import org.npt.configuration.Configuration;
import org.npt.exception.InvalidInputException;
import org.npt.exception.NotFoundException;
import org.npt.models.Target;
import org.npt.services.TargetService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TargetServiceImpl implements TargetService {

    @Override
    public Target create(String deviceName, String deviceInterface, String[] ipAddresses) throws InvalidInputException {
        Target target = new Target(deviceName,deviceInterface, new ArrayList<>(List.of(ipAddresses)),0,0,new ContextMenu());
        validate(target);
        Configuration.targets.add(target);
        return target;
    }

    @Override
    public void remove(Target target) throws NotFoundException {
        if(Configuration.targets.contains(target))
            throw new NotFoundException("Target " + target.getDeviceName() + " is not found.");
        Configuration.targets.remove(target);
    }

    @Override
    public Optional<Target> findByDeviceName(String deviceName) {
        return Configuration.targets.stream()
                .filter(target -> target.getDeviceName().equals(deviceName))
                .findAny();
    }

    @Override
    public List<Target> findByNetworkInterface(String networkInterface) {
        return Configuration.targets.stream()
                .filter(target -> target.getNetworkInterface().equals(networkInterface))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Target> findByIpAddress(String ipAddress) {
        return Configuration.targets.stream()
                .filter(target -> target.getIpAddresses().contains(ipAddress))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void validate(Target target) throws InvalidInputException {
        StringBuilder message = new StringBuilder("Error because of the following : \n");
        String deviceName = target.getDeviceName();
        if(deviceName == null && deviceName.isEmpty())
            message.append(" - Device name is empty and blank. \n");
        String networkInterface = target.getNetworkInterface();
        if(networkInterface == null && networkInterface.isEmpty())
            message.append(" - Network Interface is empty and blank. \n");
        if(target.getIpAddresses().isEmpty())
            message.append(" - IP Addresses list are empty. \n");
        throw new InvalidInputException(message.toString());
    }
}
