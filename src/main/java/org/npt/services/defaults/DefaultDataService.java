package org.npt.services.defaults;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.npt.exception.DrawNetworkException;
import org.npt.exception.InvalidInputException;
import org.npt.models.*;
import org.npt.services.DataService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultDataService implements DataService {

    private static final int EXPECTED_CAPACITY = 3000;

    @Getter
    private List<Device> devices = new ArrayList<>();

    @Getter
    private SelfDevice selfDevice;

    private static volatile DataService instance;

    public void run() throws DrawNetworkException {
        try {
            final List<Interface> interfaces = scanNetwork();
            selfDevice = new SelfDevice("Self Device", interfaces);
            devices.addAll(interfaces);
            devices.addAll(interfaces
                    .stream()
                    .filter(anInterface -> anInterface.getGatewayOptional().isPresent())
                    .map((anInterface) -> anInterface.getGatewayOptional().get())
                    .collect(Collectors.toCollection(ArrayList::new)));
        } catch (SocketException e) {
            throw new DrawNetworkException("Failed to initialize network interfaces: unable to retrieve local network data.");
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
    public Target createTarget(String deviceName, String networkInterface, String ip) throws InvalidInputException {
        Target target = new Target(deviceName, ip);
        validateTarget(target);
        Optional<Interface> selfDeviceInterfaceOpt = selfDevice.getAnInterfaces()
                .stream()
                .filter(anInterface -> anInterface.getDeviceName().equals(networkInterface))
                .findFirst();
        selfDeviceInterfaceOpt.flatMap(Interface::getGatewayOptional).ifPresent(gateway -> gateway.getDevices().add(target));
        this.addDevice(target);
        return target;
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

        final String deviceName = target.getDeviceName();
        if (deviceName == null || deviceName.trim().isEmpty()) {
            errors.put("Device Name", "Device name cannot be empty.");
        }

        devices.stream()
                .filter(device -> device.getDeviceName().equals(deviceName))
                .findFirst()
                .ifPresent(device -> errors.put("Device Name", "A device named '" + device.getDeviceName() + "' already exists."));

        final String ip = target.getIp();
        if (ip == null || ip.trim().isEmpty()) {
            errors.put("IP Address", "An IP address is required.");
        } else if (!isValidIPv4(ip)) {
            errors.put("IP Address", "Invalid IPv4 format. Please enter a valid IPv4 address (e.g., 192.168.1.1).");
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException("Input validation failed for device creation.", errors);
        }
    }

    private List<Interface> scanNetwork() throws SocketException {
        List<Interface> interfacesObj = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            final NetworkInterface ni = interfaces.nextElement();
            final String interfaceName = ni.getDisplayName();
            final Optional<String> ipAddressOptional = findFirstIPv4(ni.getInetAddresses());
            ipAddressOptional.ifPresent(ip -> {
                discoverGatewayForInterface(interfaceName).ifPresent(gw -> interfacesObj.add(new Interface(interfaceName, ip, Optional.of(gw))));
            });
        }

        return interfacesObj;
    }

    public Optional<Gateway> discoverGatewayForInterface(String interfaceName) {
        try {
            Process process = new ProcessBuilder("sh", "-c", "ip route show dev " + interfaceName).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("default")) {
                    String[] parts = line.split("\\s+");
                    int viaIndex = Arrays.asList(parts).indexOf("via");
                    if (viaIndex != -1 && viaIndex + 1 < parts.length) {
                        String gatewayIp = parts[viaIndex + 1];
                        return Optional.of(new Gateway("Router", gatewayIp, new ArrayList<>()));
                    }
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }


    private Optional<String> findFirstIPv4(Enumeration<InetAddress> ipAddresses) {
        while (ipAddresses.hasMoreElements()) {
            InetAddress address = ipAddresses.nextElement();
            if (address instanceof java.net.Inet4Address) {
                return Optional.of(address.getHostAddress());
            }
        }
        return Optional.empty();
    }

    private boolean isValidIPv4(String ip) {
        String ipv4Pattern = "^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$";
        Pattern pattern = Pattern.compile(ipv4Pattern);
        Matcher matcher = pattern.matcher(ip);

        if (matcher.matches()) {
            for (int i = 1; i <= 4; i++) {
                int part = Integer.parseInt(matcher.group(i));
                if (part < 0 || part > 255) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
