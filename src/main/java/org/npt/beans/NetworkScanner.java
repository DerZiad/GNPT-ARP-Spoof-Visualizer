package org.npt.beans;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class NetworkScanner {


    public static void scanInterfaces() {
        try {
            // Get all network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {

                // Print interface name and display name
                System.out.println("Interface Name: " + networkInterface.getName());
                System.out.println("Display Name: " + networkInterface.getDisplayName());

                // Check if the interface is up and running
                System.out.println("Is up: " + networkInterface.isUp());

                // Check if it is a loopback interface
                System.out.println("Is loopback: " + networkInterface.isLoopback());

                // Check if it is a virtual interface
                System.out.println("Is virtual: " + networkInterface.isVirtual());

                // Get and print the MAC address
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    System.out.print("MAC Address: ");
                    for (int i = 0; i < mac.length; i++) {
                        System.out.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "");
                    }
                    System.out.println();
                }

                // List all IP addresses associated with this interface
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress inetAddress : inetAddresses) {
                    System.out.println("IP Address: " + inetAddress.getHostAddress());
                }

                System.out.println("-----------------------------------");
            }
        } catch (SocketException e) {
            System.out.println("Error scanning network interfaces: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
