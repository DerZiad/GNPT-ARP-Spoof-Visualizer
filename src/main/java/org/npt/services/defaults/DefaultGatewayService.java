package org.npt.services.defaults;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.npt.exception.InvalidInputException;
import org.npt.models.Gateway;
import org.npt.models.Target;
import org.npt.services.DataService;
import org.npt.services.GatewayService;
import org.npt.services.GraphicalNetworkTracerFactory;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultGatewayService implements GatewayService {

    private static final GraphicalNetworkTracerFactory graphicalNetworkTracerFactory = GraphicalNetworkTracerFactory.getInstance();
    private static final DataService dataService = graphicalNetworkTracerFactory.getDataService();
    private static DefaultGatewayService instance = null;

    @Override
    public Gateway create(String deviceName, String deviceInterface, String[] ipAddresses, Target[] nextDevices) throws InvalidInputException {
        Gateway gateway = new Gateway(deviceName, deviceInterface, new ArrayList<>(List.of(ipAddresses)), Arrays.asList(nextDevices));
        validate(gateway);
        dataService.addDevice(gateway);
        return gateway;
    }

    @Override
    public void remove(Gateway gateway) {
        dataService.removeByObject(gateway);
    }

    @Override
    public Optional<Gateway> findByDeviceName(String deviceName) {
        HashMap<Integer, Gateway> gateways = dataService.getDevices(Gateway.class);
        for (Gateway gateway : gateways.values()) {
            if (gateway.getDeviceName().equals(deviceName)) {
                return Optional.of(gateway);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Gateway> findByTarget(Target target) {
        return dataService.getDevices(Gateway.class).values()
                .stream()
                .filter(gateway -> gateway.getDevices().contains(target))
                .findFirst();
    }

    @Override
    public HashMap<Integer, Gateway> findByNetworkInterface(String networkInterface) {
        final HashMap<Integer, Gateway> filteredMap = new HashMap<>();
        final HashMap<Integer, Gateway> allGateways = dataService.getDevices(Gateway.class);
        for (Map.Entry<Integer, Gateway> entry : allGateways.entrySet()) {
            Gateway gateway = entry.getValue();
            if (gateway.getNetworkInterface().equals(networkInterface)) {
                filteredMap.put(entry.getKey(), gateway);
            }
        }
        return filteredMap;
    }

    @Override
    public HashMap<Integer, Gateway> findByIpAddress(String ipAddress) {
        final HashMap<Integer, Gateway> filteredMap = new HashMap<>();
        final HashMap<Integer, Gateway> allGateways = dataService.getDevices(Gateway.class);
        for (Map.Entry<Integer, Gateway> entry : allGateways.entrySet()) {
            Gateway gateway = entry.getValue();
            if (gateway.getIpAddresses().contains(ipAddress)) {
                filteredMap.put(entry.getKey(), gateway);
            }
        }
        return filteredMap;
    }

    @Override
    public Collection<Gateway> find() {
        return dataService.getDevices(Gateway.class).values();
    }

    public static DefaultGatewayService getInstance() {
        if (instance == null) {
            instance = new DefaultGatewayService();
        }
        return instance;
    }

    private void validate(Gateway gateway) throws InvalidInputException {
        HashMap<String, String> errors = new HashMap<>();

        String deviceName = gateway.getDeviceName();
        if (deviceName == null || deviceName.isEmpty()) {
            errors.put("Device Name", "Device name is empty or null.");
        }

        String networkInterface = gateway.getNetworkInterface();
        if (networkInterface == null || networkInterface.isEmpty()) {
            errors.put("Network Interface", "Network Interface is empty or null.");
        }

        if (gateway.getIpAddresses() == null || gateway.getIpAddresses().isEmpty()) {
            errors.put("IP Addresses", "IP Addresses list is empty or null.");
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException("Error while validating Gateway input data", errors);
        }
    }
}
