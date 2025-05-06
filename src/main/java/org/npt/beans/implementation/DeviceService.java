package org.npt.beans.implementation;

import javafx.scene.control.ContextMenu;
import org.npt.models.Gateway;
import org.npt.models.IpAddress;
import org.npt.models.SelfDevice;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static java.util.Arrays.asList;

public class DeviceService {

    public List<Gateway> scanCurrentGateways() throws SocketException, UnknownHostException {
        List<IpAddress> ipAddresses = scanInterfaces();
        List<Gateway> gateways = new ArrayList<>();

        BiFunction<List<Gateway>, String, Optional<Gateway>> searchForSpecifiedInterface = (List<Gateway> gatewayList, String searchInterface) -> gatewayList.stream()
                .filter(gateway -> gateway.getNetworkInterface().equals(searchInterface))
                .findAny();

        for (IpAddress ipAddress : ipAddresses) {
            String ip = ipAddress.getIp();
            String interfaceName = ipAddress.getNetworkInterface();
            InetAddress inetAddr = InetAddress.getByName(ip);
            byte[] ipBytes = inetAddr.getAddress();
            ipBytes[3] = 1;
            InetAddress gatewayAddr = InetAddress.getByAddress(ipBytes);
            Optional<Gateway> gatewayOptional = searchForSpecifiedInterface.apply(gateways, interfaceName);
            Gateway gateway = gatewayOptional.orElse(null);
            if (gateway != null) {
                gateway.getIpAddresses().add(gatewayAddr.getHostAddress());
            } else {
                List<String> ipAddressesString = new ArrayList<>();
                ipAddressesString.add(gatewayAddr.getHostAddress());
                gateway = new Gateway("Router",interfaceName, ipAddressesString, 0, 0, new ContextMenu(), new ArrayList<>());
                gateways.add(gateway);
            }
        }

        return gateways;
    }

    public SelfDevice scanActualDevice(List<Gateway> gateways) throws SocketException {
        List<IpAddress> ipAddresses = scanInterfaces();
        return new SelfDevice("My Device", ipAddresses, 0, 0, new ContextMenu(), gateways);
    }

    public List<IpAddress> scanInterfaces() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        List<IpAddress> ipAddresses = new ArrayList<>();

        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            String interfaceName = ni.getDisplayName();
            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                IpAddress ipAddress = new IpAddress(addr.getHostAddress(), interfaceName);
                ipAddresses.add(ipAddress);
            }
        }
        return ipAddresses;
    }

}
