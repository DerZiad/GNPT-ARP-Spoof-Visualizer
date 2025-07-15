package org.npt.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Data
public abstract class Device implements Comparable<Device> {

    private String deviceName;

    @Override
    public int compareTo(@NotNull Device o) {
        return 0;
    }

    public boolean isValidIPv4(String ip) {
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


