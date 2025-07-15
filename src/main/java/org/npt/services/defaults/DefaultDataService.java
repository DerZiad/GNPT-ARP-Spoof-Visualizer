package org.npt.services.defaults;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.npt.exception.DrawNetworkException;
import org.npt.exception.InvalidInputException;
import org.npt.models.*;
import org.npt.services.DataService;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultDataService implements DataService {

    private static final int EXPECTED_CAPACITY = 3000;

    @Getter
    private List<Device> devices;

    @Getter
    private SelfDevice selfDevice;

    private static volatile DataService instance;

    public void run() throws DrawNetworkException {

        try {
            devices = new ArrayList<>(EXPECTED_CAPACITY);
            List<Gateway> gateways = discoverGateways();
            devices.addAll(gateways);
            selfDevice = discoverSelfDevice(gateways);
        } catch (SocketException | UnknownHostException e) {
            throw new DrawNetworkException("Error by drawing network.");
        }
    }

    @Override
    public void addDevice(@NotNull Device device) {
        devices.add(device);
    }

    @Override
    public void removeByIndex(@NotNull Integer index) {
        devices.remove(index.intValue());
    }

    @Override
    public void removeByObject(@NotNull Device device) {
        for (int i = 0; i < devices.size(); i++) {
            if (device.equals(devices.get(i))) {
                removeByIndex(i);
                break;
            }
        }
    }

    @Override
    public Device getDevice(@NotNull Integer index) {
        return devices.get(index);
    }

    @Override
    public <T> HashMap<Integer, T> getDevices(@NotNull Class<T> tClass) {
        AtomicInteger index = new AtomicInteger(0);
        return devices.stream()
                .filter(tClass::isInstance)
                .map(tClass::cast)
                .collect(Collectors.toMap(
                        i -> index.getAndIncrement(),
                        i -> i,
                        (a, b) -> b,
                        HashMap::new
                ));
    }

    @Override
    public void clear() {
        devices.clear();
    }

    @Override
    public Target createTarget(String deviceName, String deviceInterface, String[] ipAddresses) throws InvalidInputException {
        Target target = new Target(deviceName, deviceInterface, new ArrayList<>(List.of(ipAddresses)));
        validateTarget(target);
        this.addDevice(target);
        return target;
    }

    @Override
    public Gateway createGateway(String deviceName, String deviceInterface, String[] ipAddresses, Target[] nextDevices) throws InvalidInputException {
        Gateway gateway = new Gateway(deviceName, deviceInterface, new ArrayList<>(List.of(ipAddresses)), Arrays.asList(nextDevices));
        validateGateway(gateway);
        this.addDevice(gateway);
        return gateway;
    }

    @Override
    public Optional<Gateway> findGatewayByTarget(Target target) {
        return this.getDevices(Gateway.class).values()
                .stream()
                .filter(gateway -> gateway.getDevices().contains(target))
                .findFirst();
    }

    public static DataService getInstance() {
        if (instance == null) {
            synchronized (DefaultDataService.class) {
                if (instance == null) {
                    instance = new DefaultDataService();
                }
            }
        }
        return instance;
    }

    // ======================
    // Private Helper Methods
    // ======================

    private void validateTarget(Target target) throws InvalidInputException {
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

    private void validateGateway(Gateway gateway) throws InvalidInputException {
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

    private List<Gateway> discoverGateways() throws SocketException, UnknownHostException {
        List<IpAddress> interfaces = listLocalInterfaces();
        List<Gateway> gateways = new ArrayList<>();

        for (IpAddress ipAddress : interfaces) {
            String interfaceName = ipAddress.getNetworkInterface();
            String ip = ipAddress.getIp();
            InetAddress localAddress = InetAddress.getByName(ip);
            byte[] addressBytes = localAddress.getAddress();

            addressBytes[3] = 1; // Assume .1 is gateway
            String gatewayIp = InetAddress.getByAddress(addressBytes).getHostAddress();

            Gateway existing = findGatewayByInterface(gateways, interfaceName);
            if (existing != null) {
                existing.getIpAddresses().add(gatewayIp);
            } else {
                List<String> ipList = new ArrayList<>();
                ipList.add(gatewayIp);
                gateways.add(new Gateway("Router", interfaceName, ipList, new ArrayList<>()));
            }
        }

        return gateways;
    }

    private Gateway findGatewayByInterface(List<Gateway> gateways, String ifaceName) {
        for (Gateway g : gateways) {
            if (g.getNetworkInterface().equals(ifaceName)) {
                return g;
            }
        }
        return null;
    }

    private SelfDevice discoverSelfDevice(List<Gateway> gateways) throws SocketException {
        List<IpAddress> interfaces = listLocalInterfaces();
        return new SelfDevice("My Device", interfaces, gateways);
    }

    private List<IpAddress> listLocalInterfaces() throws SocketException {
        List<IpAddress> addresses = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            String ifaceName = ni.getDisplayName();

            Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress addr = inetAddresses.nextElement();
                addresses.add(new IpAddress(addr.getHostAddress(), ifaceName));
            }
        }

        return addresses;
    }
}
