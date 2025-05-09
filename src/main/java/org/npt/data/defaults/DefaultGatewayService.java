package org.npt.data.defaults;

import javafx.scene.control.ContextMenu;
import org.npt.data.DataService;
import org.npt.data.GatewayService;
import org.npt.exception.InvalidInputException;
import org.npt.models.Gateway;
import org.npt.models.Target;

import java.util.*;

public class DefaultGatewayService implements GatewayService {

    private final DataService dataService;

    public DefaultGatewayService() {
        this.dataService = DefaultDataService.getInstance();
    }

    @Override
    public Gateway create(String deviceName, String deviceInterface, String[] ipAddresses, Target[] nextDevices) throws InvalidInputException {
        Gateway gateway = new Gateway(deviceName, deviceInterface, new ArrayList<>(List.of(ipAddresses)), 0, 0, new ContextMenu(), Arrays.asList(nextDevices));
        validate(gateway);
        dataService.addDevice(Optional.of(gateway));
        return gateway;
    }

    @Override
    public void remove(Gateway gateway) {
        dataService.removeByObject(Optional.ofNullable(gateway));
    }

    @Override
    public Optional<Gateway> findByDeviceName(String deviceName) {
        HashMap<Integer, Gateway> gateways = dataService.getDevices(Optional.of(Gateway.class));
        for (Gateway gateway : gateways.values()) {
            if (gateway.getDeviceName().equals(deviceName)) {
                return Optional.of(gateway);
            }
        }
        return Optional.empty();
    }

    @Override
    public HashMap<Integer, Gateway> findByNetworkInterface(String networkInterface) {
        final HashMap<Integer, Gateway> filteredMap = new HashMap<>();
        final HashMap<Integer, Gateway> allGateways = dataService.getDevices(Optional.of(Gateway.class));
        for (Integer key : allGateways.keySet()) {
            Gateway gateway = allGateways.get(key);
            if (gateway.getNetworkInterface().equals(networkInterface)) {
                filteredMap.put(key, gateway);
            }
        }
        return filteredMap;
    }

    @Override
    public HashMap<Integer, Gateway> findByIpAddress(String ipAddress) {
        final HashMap<Integer, Gateway> filteredMap = new HashMap<>();
        final HashMap<Integer, Gateway> allGateways = dataService.getDevices(Optional.of(Gateway.class));
        for (Integer key : allGateways.keySet()) {
            Gateway gateway = allGateways.get(key);
            if (gateway.getIpAddresses().contains(ipAddress)) {
                filteredMap.put(key, gateway);
            }
        }
        return filteredMap;
    }

    @Override
    public Collection<Gateway> find() {
        return dataService.getDevices(Optional.of(Gateway.class)).values();
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
