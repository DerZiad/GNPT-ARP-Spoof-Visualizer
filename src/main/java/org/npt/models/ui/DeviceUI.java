package org.npt.models.ui;

import javafx.scene.control.ContextMenu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.npt.models.Device;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceUI {

    private double x = 0.0;
    private double y = 0.0;
    private ContextMenu contextMenu = new ContextMenu();
    private List<DeviceUI> children = new ArrayList<>();
    private Device device;

    public DeviceUI(Device device) {
        this.device = device;
    }
}
