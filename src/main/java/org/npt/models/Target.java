package org.npt.models;

import javafx.scene.control.ContextMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Target extends Device {

    @Getter
    @Setter
    private String networkInterface;

    @Getter
    @Setter
    private List<String> ipAddresses;

    public Target(String deviceName, String networkInterface, List<String> ipAddresses, double x, double y, ContextMenu contextMenu) {
        super(deviceName, x, y, contextMenu);
        this.ipAddresses = ipAddresses;
        this.networkInterface = networkInterface;
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

    public Optional<String> findFirstIPv4() {
        for (String ip : this.ipAddresses) {
            if (isValidIPv4(ip)) {
                return Optional.ofNullable(ip);
            }
        }
        return Optional.empty();
    }
}
