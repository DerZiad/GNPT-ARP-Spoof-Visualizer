package org.npt.services.defaults;

import kotlin.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.npt.exception.DrawNetworkException;
import org.npt.exception.InvalidInputException;
import org.npt.models.*;
import org.npt.services.DataService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class NetworkScanner {

    private static final int IPV4_LENGTH = 4;
    private final Interface networkInterface;

    public NetworkScanner(Interface networkInterface) {
        this.networkInterface = networkInterface;
    }

    @SneakyThrows
    public Map<String, String> scan() {
        final String cidr = buildCidr(networkInterface.getIp(), networkInterface.getNetmask());
        final ProcessBuilder builder = new ProcessBuilder("nmap", "-sn", cidr);
        builder.redirectErrorStream(true);

        final Process process = builder.start();
        final Map<String, String> devices = new LinkedHashMap<>();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            String hostname;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Nmap scan report for ")) {
                    String remainder = line.substring("Nmap scan report for ".length()).trim();
                    if (remainder.contains(" (") && remainder.endsWith(")")) {
                        int openParen = remainder.lastIndexOf(" (");
                        hostname = remainder.substring(0, openParen).trim();
                        String ip = remainder.substring(openParen + 2, remainder.length() - 1).trim();
                        if (!ip.equals(networkInterface.getIp())) {
                            devices.put(hostname, ip);
                        }
                    } else {
                        hostname = remainder;
                        if (!hostname.equals(networkInterface.getIp())) {
                            devices.put(hostname, hostname);
                        }
                    }
                }
            }
        }

        process.waitFor();
        return devices;
    }

    private String buildCidr(final String ip, final String netmask) throws IOException {
        final InetAddress ipAddr = InetAddress.getByName(ip);
        final InetAddress maskAddr = InetAddress.getByName(netmask);
        final String network = calculateNetworkAddress(ipAddr, maskAddr);
        final int prefix = netmaskToPrefix(maskAddr);
        return network + "/" + prefix;
    }

    private String calculateNetworkAddress(final InetAddress ipAddr, final InetAddress maskAddr) {
        final byte[] ipBytes = ipAddr.getAddress();
        final byte[] maskBytes = maskAddr.getAddress();
        final byte[] networkBytes = new byte[IPV4_LENGTH];

        for (int i = 0; i < IPV4_LENGTH; i++) {
            networkBytes[i] = (byte) (ipBytes[i] & maskBytes[i]);
        }

        try {
            return InetAddress.getByAddress(networkBytes).getHostAddress();
        } catch (IOException e) {
            throw new RuntimeException("Failed to calculate network address", e);
        }
    }

    private int netmaskToPrefix(final InetAddress maskAddr) {
        final byte[] bytes = maskAddr.getAddress();
        int count = 0;
        for (byte b : bytes) {
            count += Integer.bitCount(b & 0xFF);
        }
        return count;
    }
}

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultDataService implements DataService {

    @Getter
    private final List<Device> devices = new ArrayList<>();

    @Getter
    private SelfDevice selfDevice;

    private static volatile DataService instance;

    @Override
    public void run() throws DrawNetworkException {
        try {
            final List<Interface> interfaces = scanNetwork();
            selfDevice = new SelfDevice("Self Device", interfaces);
            for (final Interface anInterface : interfaces) {
                devices.add(anInterface);
                if (anInterface.getGatewayOptional().isPresent()) {
                    final Gateway gateway = anInterface.getGatewayOptional().get();
                    devices.add(gateway);
                    devices.addAll(gateway.getDevices());
                }
            }
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
        final Target target = new Target(deviceName, ip);
        validateTarget(target, networkInterface);
        final Optional<Interface> selfDeviceInterfaceOpt = selfDevice.getAnInterfaces()
                .stream()
                .filter(anInterface -> anInterface.getDeviceName().equals(networkInterface))
                .findFirst();
        selfDeviceInterfaceOpt.flatMap(Interface::getGatewayOptional).ifPresent(gateway -> {
            gateway.getDevices().add(target);
            this.addDevice(target);
        });
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

    private void validateTarget(Target target, String networkInterface) throws InvalidInputException {
        HashMap<String, String> errors = new HashMap<>();

        final String deviceName = target.getDeviceName();
        if (deviceName == null || deviceName.trim().isEmpty()) {
            errors.put("Device Name", "Device name cannot be empty.");
        }

        // check if network interface exist and the gateway associated with it is present
        Optional<Interface> interfaceOpt = Optional.empty();
        if (networkInterface == null || networkInterface.trim().isEmpty()) {
            errors.put("Network Interface", "Network interface cannot be empty.");
        } else {
            interfaceOpt = selfDevice.getAnInterfaces()
                    .stream()
                    .filter(anInterface -> anInterface.getDeviceName().equals(networkInterface))
                    .findFirst();
            if (interfaceOpt.isEmpty()) {
                errors.put("Network Interface", "The specified network interface does not exist.");
            }

            if (interfaceOpt.isPresent() && interfaceOpt.get().getGatewayOptional().isEmpty()) {
                errors.put("Network Interface", "The specified network interface does not have an associated gateway.");
            }
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

        if (interfaceOpt.isPresent() && interfaceOpt.get().getGatewayOptional().isPresent()) {
            final Gateway gateway = interfaceOpt.get().getGatewayOptional().get();
            final boolean ipExists = gateway.getDevices()
                    .stream()
                    .anyMatch(t -> t.getIp().equals(ip));
            if (ipExists) {
                errors.put("IP Address", "A Target with IP '" + ip + "' already exists for this Gateway.");
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException("Input validation failed for device creation.", errors);
        }
    }

    private List<Interface> scanNetwork() throws SocketException {
        final List<Interface> interfacesObj = new ArrayList<>();
        final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            final NetworkInterface ni = interfaces.nextElement();
            final String interfaceName = ni.getDisplayName();
            final Optional<Pair<String, String>> ipAddressOptional = findFirstIPv4(ni.getInetAddresses());
            ipAddressOptional.ifPresent(ip -> {
                final String ipString = ip.getFirst();
                final String netmaskString = ip.getSecond();
                final Optional<Gateway> gwOpt = discoverGatewayForInterface(interfaceName);
                final Interface interfaceObj = new Interface(interfaceName, ipString, netmaskString, gwOpt);
                if (gwOpt.isPresent()) {
                    final NetworkScanner networkScanner = new NetworkScanner(interfaceObj);
                    final Map<String, String> foundIps = networkScanner.scan();
                    final Gateway gw = gwOpt.get();
                    for (final String hostname : foundIps.keySet()) {
                        final String foundIp = foundIps.get(hostname);
                        if (!gw.getIp().equals(foundIp)) {
                            final Target target = new Target(hostname, foundIp);
                            gw.getDevices().add(target);
                        }
                    }
                }
                interfacesObj.add(new Interface(interfaceName, ipString, netmaskString, gwOpt));
            });
        }

        return interfacesObj;
    }

    private Optional<Gateway> discoverGatewayForInterface(String interfaceName) {
        try {
            final Process process = new ProcessBuilder("sh", "-c", "ip route show dev " + interfaceName).start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("default")) {
                    final String[] parts = line.split("\\s+");
                    final int viaIndex = Arrays.asList(parts).indexOf("via");
                    if (viaIndex != -1 && viaIndex + 1 < parts.length) {
                        final String gatewayIp = parts[viaIndex + 1];
                        return Optional.of(new Gateway("Router", gatewayIp, new ArrayList<>()));
                    }
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<Pair<String, String>> findFirstIPv4(Enumeration<InetAddress> ipAddresses) {
        while (ipAddresses.hasMoreElements()) {
            final InetAddress address = ipAddresses.nextElement();
            if (address instanceof Inet4Address) {
                try {
                    final NetworkInterface ni = NetworkInterface.getByInetAddress(address);
                    if (ni != null) {
                        for (final InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                            if (interfaceAddress.getAddress() instanceof Inet4Address) {
                                final short prefix = interfaceAddress.getNetworkPrefixLength(); // e.g., 24
                                final String netmask = prefixLengthToNetmask(prefix);
                                return Optional.of(new Pair<>(address.getHostAddress(), netmask));
                            }
                        }
                    }
                } catch (SocketException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private String prefixLengthToNetmask(int prefixLength) {
        final int mask = 0xffffffff << (32 - prefixLength);
        return String.format("%d.%d.%d.%d",
                (mask >>> 24) & 0xff,
                (mask >>> 16) & 0xff,
                (mask >>> 8) & 0xff,
                mask & 0xff);
    }

    private boolean isValidIPv4(String ip) {
        final String ipv4Pattern = "^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$";
        final Pattern pattern = Pattern.compile(ipv4Pattern);
        final Matcher matcher = pattern.matcher(ip);

        if (matcher.matches()) {
            for (int i = 1; i <= 4; i++) {
                final int part = Integer.parseInt(matcher.group(i));
                if (part < 0 || part > 255) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
