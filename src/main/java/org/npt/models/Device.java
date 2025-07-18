package org.npt.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Data
@EqualsAndHashCode
public abstract class Device implements Comparable<Device> {

    private String deviceName;

    @Override
    public int compareTo(@NotNull Device o) {
        return 0;
    }
}


