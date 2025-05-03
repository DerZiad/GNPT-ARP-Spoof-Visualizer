package org.npt.beans.implementation;

import javafx.scene.control.ContextMenu;
import org.npt.models.Device;
import org.npt.models.Gateway;
import org.npt.models.IpAddress;
import org.npt.models.SelfDevice;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class DeviceService {

    public List<Gateway> scanCurrentGateways() throws SocketException, UnknownHostException {
        List<IpAddress> ipAddresses = scanInterfaces();
        List<Gateway> gateways = new ArrayList<>();

        for (IpAddress ipAddress : ipAddresses) {
                String ip = ipAddress.getIp();
                String interfaceName = ipAddress.getNetworkInterface();
                InetAddress inetAddr = InetAddress.getByName(ip);
                byte[] ipBytes = inetAddr.getAddress();
                ipBytes[3] = 1;
                InetAddress gatewayAddr = InetAddress.getByAddress(ipBytes);
                List<IpAddress> gatewayIp = List.of(new IpAddress(gatewayAddr.getHostAddress(), interfaceName));
                Gateway gateway = new Gateway("Router", gatewayIp, 0, 0, new ContextMenu(), new ArrayList<>());
                gateways.add(gateway);
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
