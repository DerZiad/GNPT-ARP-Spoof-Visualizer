package org.npt.services.impl;

import org.npt.services.GatewayService;

public abstract class GatewayServiceImpl implements GatewayService {
    /*
    @Override
    public Gateway create(String deviceName, String deviceInterface, String[] ipAddresses) throws InvalidInputException {
        return null;
    }

    @Override
    public void remove(Gateway gateway) throws NotFoundException {

    }

    @Override
    public Optional<Gateway> findByDeviceName(String deviceName) {
        return Optional.empty();
    }

    @Override
    public List<Gateway> findByNetworkInterface(String networkInterface) {
        return List.of();
    }

    @Override
    public List<Gateway> findByIpAddress(String ipAddress) {
        return Configuration.gateways.stream()
                .filter(target -> target.getIpAddresses().contains(ipAddress))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void validate(Gateway gateway) throws InvalidInputException {
        StringBuilder message = new StringBuilder("Error because of the following : \n");
        String deviceName = gateway.getDeviceName();
        if(deviceName == null && deviceName.isEmpty())
            message.append(" - Device name is empty and blank. \n");
        String networkInterface = gateway.getNetworkInterface();
        if(networkInterface == null && networkInterface.isEmpty())
            message.append(" - Network Interface is empty and blank. \n");
        if(gateway.getIpAddresses().isEmpty())
            message.append(" - IP Addresses list are empty. \n");
        throw new InvalidInputException(message.toString());
    }*/
}
