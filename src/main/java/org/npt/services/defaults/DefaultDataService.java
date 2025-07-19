package org.npt.services.defaults;

import kotlin.Pair;
import lombok.Getter;
import org.npt.exception.DrawNetworkException;
import org.npt.exception.InvalidInputException;
import org.npt.models.*;
import org.npt.services.DataService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class DefaultDataService implements DataService {

    private final HashMap<Interface, Pair<Thread, NetworkScanner>> networkScanners = new HashMap<>();

    @Getter
    private SelfDevice selfDevice;

    private static volatile DataService instance;

    private DefaultDataService() {
        selfDevice = new SelfDevice(getCurrentDeviceName());
    }

    @Override
    public void run() throws DrawNetworkException {
        try {
            scanNetwork();
        } catch (SocketException e) {
            throw new DrawNetworkException("Failed to initialize network interfaces: unable to retrieve local network data.");
        }
    }

    @Override
    public Target createTarget(String deviceName, String networkInterface, String ip) throws InvalidInputException {
        final Target target = new Target(deviceName, ip);
        validateTarget(target, networkInterface);
        final Interface targetInterface = selfDevice.getInterfaceIfExist(networkInterface).get();
        final Gateway gateway = targetInterface.getGateway();
        gateway.getDevices().add(target);
        return target;
    }

    @Override
    public Optional<Interface> findInterfaceByTarget(Target target) {
        return selfDevice.getAnInterfaces()
                .stream()
                .filter(anInterface -> anInterface.targetAlreadyScanned(target))
                .findFirst();
    }

    @Override
    public void remove(Device device) {
        // TODO : Implement the logic to remove a device from the selfDevice's interfaces or gateways.
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

    // ==================================================== //
    // Private Helper Methods // Network scanning methods   //
    // ==================================================== //

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

            if (interfaceOpt.isPresent() && interfaceOpt.get().getGateway() == null) {
                errors.put("Network Interface", "The specified network interface does not have an associated gateway.");
            }
        }

        final String ip = target.getIp();
        if (ip == null || ip.trim().isEmpty()) {
            errors.put("IP Address", "An IP address is required.");
        } else if (!isValidIPv4(ip)) {
            errors.put("IP Address", "Invalid IPv4 format. Please enter a valid IPv4 address (e.g., 192.168.1.1).");
        }

        if (interfaceOpt.isPresent()) {
            final Interface anInterface = interfaceOpt.get();
            if (anInterface.targetAlreadyScanned(target.getIp())) {
                errors.put("IP Address", "A Target with IP '" + ip + "' already exists for this Gateway.");
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException("Input validation failed for device creation.", errors);
        }
    }

    private void scanNetwork() throws SocketException {
        final List<Interface> interfacesObj = new ArrayList<>();
        final Set<NetworkInterface> networkInterface = discoverNetworkInterfaces();
        final Set<Interface> existingInterfaceNames = selfDevice.getAnInterfaces()
                .stream()
                .collect(Collectors.toSet());
        // Delete interfaces that are not present in the current scan
        for (Interface existingInterface : existingInterfaceNames) {
            if (networkInterface.stream().noneMatch(ni -> ni.getDisplayName().equals(existingInterface.getDeviceName()))) {
                selfDevice.getAnInterfaces().remove(existingInterface);
            }
        }

        // Add or update interfaces
        for (NetworkInterface ni : networkInterface) {
            final String displayName = ni.getDisplayName();
            final Optional<Interface> createdInterface = addInterfaceOrEmptyIfExist(ni);
            createdInterface.ifPresent(createdInterfaceObj -> selfDevice.getAnInterfaces().add(createdInterface.get()));
        }

        // Scan for targets on each interface
        for (final Interface computedInterface : selfDevice.getAnInterfaces()) {
            if (computedInterface.equals("lo") || (networkScanners.containsKey(computedInterface) && networkScanners.get(computedInterface).getFirst().isAlive()))
                continue;
            final NetworkScanner networkScanner = new NetworkScanner(computedInterface);
            final Thread scannerThread = new Thread(networkScanner, "NetworkScanner-" + computedInterface.getDeviceName());
            scannerThread.start();
            networkScanners.put(computedInterface, new Pair<>(scannerThread, networkScanner));
        }
    }

    private Optional<Interface> addInterfaceOrEmptyIfExist(final NetworkInterface ni) {
        final String interfaceName = ni.getDisplayName();
        final Optional<Pair<String, String>> ipAddressOptional = findFirstIPv4(ni.getInetAddresses());
        if (ipAddressOptional.isEmpty())
            return Optional.empty();
        final String ipString = ipAddressOptional.get().getFirst();
        final Optional<Interface> anInterface = selfDevice.getInterfaceIfExist(ni.getDisplayName());
        final String netmaskString = ipAddressOptional.get().getSecond();
        final Gateway gateway = discoverGateway(interfaceName);
        if (anInterface.isPresent()) {
            final Interface existingInterface = anInterface.get();
            existingInterface.setIp(ipString);
            if (existingInterface.getGateway() != null) {
                existingInterface.getGateway().setIp(ipString);
            }
            existingInterface.setNetmask(netmaskString);
            return Optional.empty();
        }
        final Interface interfaceObj = new Interface(interfaceName, ipString, netmaskString, gateway);
        if (gateway == null)
            return Optional.of(interfaceObj);
        return Optional.of(interfaceObj);
    }

    private Gateway discoverGateway(String interfaceName) {
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
                        return new Gateway("Router", gatewayIp);
                    }
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private Set<NetworkInterface> discoverNetworkInterfaces() throws SocketException {
        final Set<NetworkInterface> networkInterfaces = new HashSet<>();
        final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            final NetworkInterface ni = interfaces.nextElement();
            if (ni.isUp() && !ni.isLoopback() && !ni.isVirtual() && ni.supportsMulticast()) {
                networkInterfaces.add(ni);
            }
        }
        return networkInterfaces;
    }

    private String getCurrentDeviceName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Device";
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
