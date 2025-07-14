package org.npt.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KnownHost {

    private String name;

    private String iconPath;

    private List<String> ips;

    public boolean containsIp(String ipAddress) {
        int targetIp = ipToInt(ipAddress);
        for (String cidr : ips) {
            if (isInRange(targetIp, cidr)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInRange(int ip, String cidr) {
        String[] parts = cidr.trim().split("/");
        if (parts.length != 2) return false;

        int baseIp = ipToInt(parts[0]);
        int prefixLength = Integer.parseInt(parts[1]);

        int mask = ~((1 << (32 - prefixLength)) - 1);
        return (ip & mask) == (baseIp & mask);
    }

    private int ipToInt(String ipAddress) {
        try {
            byte[] bytes = InetAddress.getByName(ipAddress).getAddress();
            int result = 0;
            for (byte b : bytes) {
                result = (result << 8) | (b & 0xFF);
            }
            return result;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
        }
    }
}
