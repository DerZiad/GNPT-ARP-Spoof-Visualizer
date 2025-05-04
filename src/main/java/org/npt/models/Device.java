package org.npt.models;

import javafx.scene.control.ContextMenu;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
@Data
public abstract class Device implements Comparable<Device> {

    private String deviceName;
    private List<IpAddress> ipAddresses;
    private double x;
    private double y;
    private ContextMenu contextMenu;

    @Override
    public int compareTo(@NotNull Device o) {
        return 0;
    }
}


