package org.npt.uiservices;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import org.npt.controllers.View;
import org.npt.controllers.viewdetails.GatewayDetailsController;
import org.npt.controllers.viewdetails.SelfDeviceDetailsController;
import org.npt.controllers.viewdetails.TargetDetailsController;
import org.npt.exception.NotFoundException;
import org.npt.models.Gateway;
import org.npt.models.SelfDevice;
import org.npt.models.Target;
import org.npt.models.ui.DeviceUI;
import org.npt.services.ArpSpoofService;
import org.npt.services.DataService;
import org.npt.services.GatewayService;
import org.npt.services.GraphicalNetworkTracerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.npt.controllers.View.getFxmlResourceAsExternalForm;

public class DeviceUiMapperService {

    private static final GraphicalNetworkTracerFactory graphicalNetworkTracerFactory = GraphicalNetworkTracerFactory.getInstance();
    private static final DataService dataService = graphicalNetworkTracerFactory.getDataService();
    private static final ArpSpoofService arpSpoofService = graphicalNetworkTracerFactory.getArpSpoofService();
    private static final GatewayService gatewayService = graphicalNetworkTracerFactory.getGatewayService();

    private static final String START_SPOOFING_TEXT = "Start Spoofing";
    private static final String STOP_SPOOFING_TEXT = "Stop Spoofing";
    private static final String SHOW_DETAILS_TEXT = "View Details";
    private static final String REMOVE_DEVICE_TEXT = "Remove Device";
    private static final String SPY_TEXT = "Spy";

    private final Runnable refreshAction;

    @Getter
    private final DeviceUI selfDevice;

    @Getter
    private final List<DeviceUI> devices = new ArrayList<>();

    public DeviceUiMapperService(Runnable refreshAction) {
        this.refreshAction = refreshAction;
        selfDevice = new DeviceUI(dataService.getSelfDevice());
        initMenu(selfDevice);
        dataService.getDevices().forEach(device -> {
            DeviceUI deviceUI = new DeviceUI(device);
            initMenu(deviceUI);
            devices.add(deviceUI);
        });
    }

    public <T> List<DeviceUI> findAll(Class<T> clazz) {
        return devices.stream()
                .filter(Objects::nonNull)
                .filter(deviceUi -> clazz.isInstance(deviceUi.getDevice()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Privates functions
    private void initMenu(DeviceUI deviceUI) {
        ContextMenu contextMenu = deviceUI.getContextMenu();
        MenuItem detailsItem = new MenuItem(SHOW_DETAILS_TEXT);
        detailsItem.setOnAction(ignored -> showDetails(deviceUI, refreshAction));

        MenuItem removeItem = new MenuItem(REMOVE_DEVICE_TEXT);
        removeItem.setOnAction(ignored -> {
            devices.remove(deviceUI);
            dataService.removeByObject(deviceUI.getDevice());
            refreshAction.run();
        });

        if (deviceUI.getDevice() instanceof Target) {
            MenuItem startSpoofingMenuItem = new MenuItem(START_SPOOFING_TEXT);
            startSpoofingMenuItem.setOnAction(ignored -> {
                try {
                    if (startSpoofingMenuItem.getText().equals(START_SPOOFING_TEXT)) {
                        spoof(deviceUI);
                        startSpoofingMenuItem.setText(STOP_SPOOFING_TEXT);
                        MenuItem menuItem = new MenuItem(SPY_TEXT);
                        // TODO switch to Statistic Scene menuItem.setOnAction(ignored -> Launch.StageSwitcher.switchTo(View.STATISTICS_DETAILS_VIEW.FXML_FILE, View.STATISTICS_DETAILS_VIEW.WIDTH, View.STATISTICS_DETAILS_VIEW.HEIGHT, View.STATISTICS_DETAILS_VIEW.INTERFACE_TITLE, target));
                        contextMenu.getItems().add(menuItem);
                    } else {
                        stop(deviceUI);
                        startSpoofingMenuItem.setText(START_SPOOFING_TEXT);
                        contextMenu.getItems().removeIf(menuItem -> menuItem.getText().equals(SPY_TEXT));
                    }
                    refreshAction.run();
                } catch (NotFoundException ex) {
                    PopupShowDetails.showError("Error while spoofing", ex.getMessage(), true);
                }
            });
            contextMenu.getItems().add(startSpoofingMenuItem);
        }
        contextMenu.getItems().addAll(detailsItem, removeItem);
    }

    private void spoof(DeviceUI deviceUI) throws NotFoundException {
        Target target = (Target) deviceUI.getDevice();
        Gateway gateway = gatewayService.findByTarget(target)
                .orElseThrow(() -> new NotFoundException("Couldn't spoof a target that it is not connected to the same Network"));
        String scanInterface = target.getNetworkInterface();
        arpSpoofService.spoof(scanInterface, target, gateway);
    }

    private void stop(DeviceUI deviceUI) throws NotFoundException {
        Target target = (Target) deviceUI.getDevice();
        Gateway gateway = gatewayService.findByTarget(target)
                .orElseThrow(() -> new NotFoundException("During search to stop spoofing, the target was not found in the gateways list, this may be due to the target not being connected to the same network as the gateway or disconnected from the network."));
        arpSpoofService.stop(target, gateway);
    }

    // TODO use FrameService to switch to StatisticsController and to PopupShowDetails

    private <T> void showDetails(T object, Runnable refresh) {
        if (object instanceof Target target) {
            PopupShowDetails.popupShowDetails(target, refresh);
        } else if (object instanceof SelfDevice selfDevice) {
            PopupShowDetails.popupShowDetails(selfDevice, refresh);
        } else {
            Gateway gateway = (Gateway) object;
            PopupShowDetails.popupShowDetails(gateway, refresh);
        }
    }

    public static class PopupShowDetails {

        public static void popupShowDetails(Target target, Runnable refresh) {
            try {
                FXMLLoader loader = new FXMLLoader(getFxmlResourceAsExternalForm(View.TARGET_DETAILS_VIEW.FXML_FILE));
                Parent root = loader.load();
                TargetDetailsController controller = loader.getController();
                controller.setData(target, refresh);
                Stage popupStage = new Stage();
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.setTitle(View.TARGET_DETAILS_VIEW.INTERFACE_TITLE);
                popupStage.setWidth(View.TARGET_DETAILS_VIEW.WIDTH);
                popupStage.setHeight(View.TARGET_DETAILS_VIEW.HEIGHT);
                popupStage.setResizable(false);
                popupStage.setScene(new Scene(root));
                popupStage.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public static void popupShowDetails(Gateway gateway, Runnable refresh) {
            try {
                FXMLLoader loader = new FXMLLoader(getFxmlResourceAsExternalForm(View.GATEWAY_DETAILS_VIEW.FXML_FILE));
                Parent root = loader.load();
                GatewayDetailsController controller = loader.getController();
                controller.setData(gateway, refresh);
                Stage popupStage = new Stage();
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.setTitle(View.GATEWAY_DETAILS_VIEW.INTERFACE_TITLE);
                popupStage.setWidth(View.GATEWAY_DETAILS_VIEW.WIDTH);
                popupStage.setHeight(View.GATEWAY_DETAILS_VIEW.HEIGHT);
                popupStage.setResizable(false);
                popupStage.setScene(new Scene(root));
                popupStage.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public static void popupShowDetails(SelfDevice selfDevice, Runnable refresh) {
            try {
                FXMLLoader loader = new FXMLLoader(getFxmlResourceAsExternalForm(View.SELF_DEVICE_DETAILS_VIEW.FXML_FILE));
                Parent root = loader.load();
                SelfDeviceDetailsController controller = loader.getController();
                controller.setData(selfDevice, refresh);
                Stage popupStage = new Stage();
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.setTitle(View.SELF_DEVICE_DETAILS_VIEW.INTERFACE_TITLE);
                popupStage.setWidth(View.SELF_DEVICE_DETAILS_VIEW.WIDTH);
                popupStage.setHeight(View.SELF_DEVICE_DETAILS_VIEW.HEIGHT);
                popupStage.setResizable(false);
                popupStage.setScene(new Scene(root));
                popupStage.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public static void showError(String title, String message, Boolean showAndWait) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("An error occurred");
            alert.setHeaderText(title);
            alert.setContentText(message);
            if (showAndWait) alert.showAndWait();
        }
    }
}
