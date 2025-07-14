package org.npt.services.defaults;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.npt.exception.InvalidInputException;
import org.npt.models.Target;
import org.npt.services.DataService;
import org.npt.services.GraphicalNetworkTracerFactory;
import org.npt.services.TargetService;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultTargetService implements TargetService {

    private final GraphicalNetworkTracerFactory graphicalNetworkTracerFactory = GraphicalNetworkTracerFactory.getInstance();
    private final DataService dataService = graphicalNetworkTracerFactory.getDataService();
    private static DefaultTargetService instance = null;

    @Override
    public Target create(final String deviceName, final String deviceInterface, final String[] ipAddresses) throws InvalidInputException {
        Target target = new Target(deviceName, deviceInterface, new ArrayList<>(List.of(ipAddresses)));
        validate(target);
        dataService.addDevice(target);
        return target;
    }

    @Override
    public void remove(final Target target) {
        dataService.removeByObject(target);
    }

    @Override
    public Optional<Target> findByDeviceName(final String deviceName) {
        HashMap<Integer, Target> targets = dataService.getDevices(Target.class);
        for (Target target : targets.values()) {
            if (target.getDeviceName().equals(deviceName)) {
                return Optional.of(target);
            }
        }
        return Optional.empty();
    }

    @Override
    public HashMap<Integer, Target> findByNetworkInterface(final String networkInterface) {
        final HashMap<Integer, Target> filteredHashMap = new HashMap<>();
        final HashMap<Integer, Target> allTargetHashMap = dataService.getDevices(Target.class);
        for (Map.Entry<Integer, Target> entry : allTargetHashMap.entrySet()) {
            Target target = entry.getValue();
            if (target.getNetworkInterface().equals(networkInterface)) {
                filteredHashMap.put(entry.getKey(), target);
            }
        }
        return filteredHashMap;
    }

    @Override
    public HashMap<Integer, Target> findByIpAddress(final String ipAddress) {
        final HashMap<Integer, Target> filteredHashMap = new HashMap<>();
        final HashMap<Integer, Target> allTargetHashMap = dataService.getDevices(Target.class);
        for (Map.Entry<Integer, Target> entry : allTargetHashMap.entrySet()) {
            Target target = entry.getValue();
            if (target.getIpAddresses().contains(ipAddress)) {
                filteredHashMap.put(entry.getKey(), target);
            }
        }
        return filteredHashMap;
    }

    @Override
    public Collection<Target> find() {
        return dataService.getDevices(Target.class).values();
    }

    public static DefaultTargetService getInstance() {
        if (instance == null) {
            instance = new DefaultTargetService();
        }
        return instance;
    }

    private void validate(Target target) throws InvalidInputException {
        HashMap<String, String> errors = new HashMap<>();

        String deviceName = target.getDeviceName();
        if (deviceName == null || deviceName.isEmpty()) {
            errors.put("Device Name", "Device name is empty and blank.");
        }

        String networkInterface = target.getNetworkInterface();
        if (networkInterface == null || networkInterface.isEmpty()) {
            errors.put("Network Interface", "Network Interface is empty and blank.");
        }

        List<String> ipAddresses = target.getIpAddresses();
        if (ipAddresses == null || ipAddresses.isEmpty()) {
            errors.put("IP Addresses", "IP Addresses list is empty.");
        } else if (target.findFirstIPv4().isEmpty()) {
            errors.put("IP Addresses", "In order to perform ARP spoofing, an IPv4 address must be present.");
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException("Error while validating input data", errors);
        }
    }
}
