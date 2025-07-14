package org.npt.services.defaults;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.npt.exception.DrawNetworkException;
import org.npt.models.Device;
import org.npt.models.Gateway;
import org.npt.models.IpAddress;
import org.npt.models.SelfDevice;
import org.npt.services.DataService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        }catch (SocketException | UnknownHostException e){
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
