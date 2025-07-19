package org.npt.services.defaults;

import lombok.SneakyThrows;
import org.npt.models.Interface;
import org.npt.models.Target;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NetworkScanner implements Runnable {

    private static final int IPV4_LENGTH = 4;
    private final Interface networkInterface;

    public NetworkScanner(Interface networkInterface) {
        this.networkInterface = networkInterface;
    }

    @SneakyThrows
    public Map<String, String> scan() {
        final String cidr = buildCidr(networkInterface.getIp(), networkInterface.getNetmask());
        final ProcessBuilder builder = new ProcessBuilder("nmap", "-sn", "-T5", cidr);
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

    @Override
    public void run() {
        final Map<String, String> foundIps = scan();
        for (final String hostname : foundIps.keySet()) {
            final String foundIp = foundIps.get(hostname);
            if (!networkInterface.targetAlreadyScanned(foundIp)) {
                final Target target = new Target(hostname, foundIp);
                if (!networkInterface.targetAlreadyScanned(target) && networkInterface.getGateway() != null) {
                    networkInterface.getGateway().getDevices().add(target);
                }
            }
        }
    }
}
