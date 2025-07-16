package org.npt.models.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.npt.controllers.MainController;
import org.npt.controllers.StatisticsController;
import org.npt.controllers.viewdetails.GatewayDetailsController;
import org.npt.controllers.viewdetails.SelfDeviceDetailsController;
import org.npt.controllers.viewdetails.TargetDetailsController;

@AllArgsConstructor
@Data
public class Frame {

    private static final String PREFIX = "org/npt";

    private String key;
    private String title;
    private String fxmlLocation;
    private Class<?> controllerClass;
    private Object[] args;
    private Size size;

    public record Size(double width, double height) {

    }

    public static Frame createMainFrame() {
        return new Frame(
                "mainFrame",
                "Network Packet Tracer",
                PREFIX + "/main_frame.fxml",
                MainController.class,
                new Object[]{},
                new Size(1054.0, 674.0)
        );
    }

    public static Frame createTargetView() {
        return new Frame(
                "targetFrame",
                "Target Details",
                PREFIX + "/target_frame.fxml",
                TargetDetailsController.class,
                new Object[]{},
                new Size(689, 323)
        );
    }

    public static Frame createGatewayDetails() {
        return new Frame(
                "gatewayDetails",
                "Gateway Details",
                PREFIX + "/gateway_frame.fxml",
                GatewayDetailsController.class,
                new Object[]{},
                new Size(722.0, 490.0)
        );
    }

    public static Frame createSelfDetails() {
        return new Frame(
                "selfDeviceDetails",
                "Self Device Details",
                PREFIX + "/selfdevice_frame.fxml",
                SelfDeviceDetailsController.class,
                new Object[]{},
                new Size(689, 433)
        );
    }

    public static Frame createStatisticsDetails() {
        return new Frame(
                "showDetailsFrame",
                "Show Details",
                PREFIX + "/showdetails_frame.fxml",
                StatisticsController.class,
                new Object[]{},
                new Size(1054, 674)
        );
    }
}