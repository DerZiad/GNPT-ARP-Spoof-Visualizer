package org.npt.data.defaults;


import javafx.scene.control.ContextMenu;
import org.npt.configuration.Configuration;
import org.npt.data.DataService;
import org.npt.exception.InvalidInputException;
import org.npt.exception.NotFoundException;
import org.npt.models.Target;
import org.npt.data.TargetService;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultTargetService implements TargetService {

    private final DataService dataService;

    public DefaultTargetService() {
        dataService = DefaultDataService.getInstance();
    }

    @Override
    public Target create(final String deviceName, final String deviceInterface, final String[] ipAddresses) throws InvalidInputException {
        Target target = new Target(deviceName,deviceInterface, new ArrayList<>(List.of(ipAddresses)),0,0,new ContextMenu());
        validate(target);
        dataService.addDevice(Optional.of(target));
        return target;
    }

    @Override
    public void remove(final Target target) {
        dataService.removeByObject(Optional.ofNullable(target));
    }

    @Override
    public Optional<Target> findByDeviceName(final String deviceName) {
        HashMap<Integer, Target> targets = dataService.getDevices(Optional.of(Target.class));
        for (Target target:targets.values()){
            if(target.getDeviceName().equals(deviceName))
                return Optional.of(target);
        }
        return Optional.empty();
    }

    @Override
    public HashMap<Integer, Target> findByNetworkInterface(final String networkInterface) {
        final HashMap<Integer, Target> filteredHashMap = new HashMap<>();
        final HashMap<Integer, Target> allTargetHashMap = dataService.getDevices(Optional.of(Target.class));
        for(Integer key:allTargetHashMap.keySet()){
            Target target = allTargetHashMap.get(key);
            if(target.getNetworkInterface().equals(networkInterface))
                filteredHashMap.put(key,target);
        }
        return filteredHashMap;
    }

    @Override
    public HashMap<Integer, Target> findByIpAddress(final String ipAddress) {
        final HashMap<Integer, Target> filteredHashMap = new HashMap<>();
        final HashMap<Integer, Target> allTargetHashMap = dataService.getDevices(Optional.of(Target.class));
        for(Integer key:allTargetHashMap.keySet()){
            Target target = allTargetHashMap.get(key);
            if(target.getIpAddresses().contains(ipAddress))
                filteredHashMap.put(key,target);
        }
        return filteredHashMap;
    }

    @Override
    public Collection<Target> find() {
        return dataService.getDevices(Optional.of(Target.class)).values();
    }

    private void validate(Target target) throws InvalidInputException {
        HashMap<String,String> errors = new HashMap<>();
        String deviceName = target.getDeviceName();
        if(deviceName == null && deviceName.isEmpty())
            errors.put("Device Name","Device name is empty and blank.");
        String networkInterface = target.getNetworkInterface();
        if(networkInterface == null && networkInterface.isEmpty())
            errors.put("Network Interface","Network Interface is empty and blank.");
        if(target.getIpAddresses().isEmpty())
            errors.put("IP Addresses", "IP Addresses list are empty.");

        if(!errors.isEmpty())
            throw new InvalidInputException("Error while validating input data", errors);
    }
}
