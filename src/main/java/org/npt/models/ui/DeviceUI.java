package org.npt.models.ui;

import javafx.scene.control.ContextMenu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.npt.models.Device;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceUI {

    private Device device;
    private double x = 0.0;
    private double y = 0.0;
    private ContextMenu contextMenu = new ContextMenu();

    public DeviceUI(Device device) {
        this.device = device;
    }
}
