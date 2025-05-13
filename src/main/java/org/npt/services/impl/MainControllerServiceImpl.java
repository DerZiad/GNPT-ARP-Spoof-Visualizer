package org.npt.services.impl;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.npt.Launch;
import org.npt.controllers.View;
import org.npt.controllers.viewdetails.GatewayDetailsController;
import org.npt.controllers.viewdetails.SelfDeviceDetailsController;
import org.npt.controllers.viewdetails.TargetDetailsController;
import org.npt.data.DataService;
import org.npt.data.GatewayService;
import org.npt.data.defaults.DefaultDataService;
import org.npt.data.defaults.DefaultGatewayService;
import org.npt.exception.GatewayException;
import org.npt.exception.TargetException;
import org.npt.exception.children.GatewayIpException;
import org.npt.exception.children.GatewayNotFoundException;
import org.npt.exception.children.TargetIpException;
import org.npt.models.Device;
import org.npt.models.Gateway;
import org.npt.models.SelfDevice;
import org.npt.models.Target;
import org.npt.networkservices.ArpSpoofStarter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.npt.controllers.View.getFxmlResourceAsExternalForm;

public class MainControllerServiceImpl {

    private final ArpSpoofStarter arpSpoofStarter = ArpSpoofStarter.getInstance();

    private final DataService dataService = DefaultDataService.getInstance();

    private final GatewayService gatewayService = new DefaultGatewayService();

    @Getter
    private final Set<Target> devices = new HashSet<>();

    private void spoof(Target target) throws GatewayNotFoundException, TargetIpException, GatewayIpException {
        Optional<Gateway> gatewayOptional = gatewayService.find().stream().filter(gateway -> gateway.getDevices().contains(target)).findAny();
        Gateway gateway = gatewayOptional.orElseThrow(() -> new GatewayNotFoundException("Couldn't spoof a target that it is not connected"));
        String scanInterface = target.getNetworkInterface();
        String targetIpAddress = target.findFirstIPv4().orElseThrow(() -> new TargetIpException("No IpV4 found for target " + target.getDeviceName()));
        String gatewayIpAddress = gateway.findFirstIPv4().orElseThrow(() -> new GatewayIpException("No IpV4 found for gateway " + gateway.getDeviceName()));
        arpSpoofStarter.startSpoofing(scanInterface, target, gatewayIpAddress);
    }

    private void stopSpoofing(Target target) throws TargetException, GatewayException {
        Optional<Gateway> gatewayOptional = gatewayService.find().stream().filter(gateway -> gateway.getDevices().contains(target)).findAny();
        Gateway gateway = gatewayOptional.orElseThrow(() -> new GatewayNotFoundException("Couldn't spoof a target that it is not connected"));
        String targetIpAddress = target.findFirstIPv4().orElseThrow(() -> new TargetIpException("No IpV4 found for target " + target.getDeviceName()));
        String gatewayIpAddress = gateway.findFirstIPv4().orElseThrow(() -> new GatewayIpException("No IpV4 found for gateway " + gateway.getDeviceName()));
        arpSpoofStarter.stopSpoofing(targetIpAddress, gatewayIpAddress);
    }

    public void initMenu(Device device, Runnable refresh) {
        ContextMenu contextMenu = device.getContextMenu();
        MenuItem detailsItem = new MenuItem("View Details");
        detailsItem.setOnAction(_ -> showDetails(device, refresh));

        MenuItem removeItem = new MenuItem("Remove Device");
        removeItem.setOnAction(_ -> {
            dataService.removeByObject(Optional.of(device));
            refresh.run();
        });

        if (device instanceof Target) {
            MenuItem startSpoofingMenuItem = configureMenuItem(device, refresh, contextMenu);
            contextMenu.getItems().add(startSpoofingMenuItem);
        }
        contextMenu.getItems().addAll(detailsItem, removeItem);
    }

    @NotNull
    private MenuItem configureMenuItem(Device device, Runnable refresh, ContextMenu contextMenu) {
        MenuItem startSpoofingMenuItem = new MenuItem("Start Spoofing");
        startSpoofingMenuItem.setOnAction(e -> {
            try {
                Target target = (Target) device;
                if (startSpoofingMenuItem.getText().equals("Start Spoofing")) {
                    spoof(target);
                    devices.add(target);
                    refresh.run();
                    startSpoofingMenuItem.setText("Stop Spoofing");
                    MenuItem menuItem = new MenuItem("Spy");
                    menuItem.setOnAction(_ -> {
                        Launch.StageSwitcher.switchTo(View.STATISTICS_DETAILS_VIEW.FXML_FILE, View.STATISTICS_DETAILS_VIEW.WIDTH, View.STATISTICS_DETAILS_VIEW.HEIGHT, View.STATISTICS_DETAILS_VIEW.INTERFACE_TITLE, target);
                    });
                    contextMenu.getItems().add(menuItem);
                } else {
                    stopSpoofing(target);
                    startSpoofingMenuItem.setText("Start Spoofing");
                    devices.remove(target);
                    refresh.run();
                    int i;
                    for (i = 0; i < contextMenu.getItems().size(); i++) {
                        MenuItem menuItem = contextMenu.getItems().get(i);
                        if (menuItem.getText().equals("Spy")) {
                            contextMenu.getItems().remove(i);
                            break;
                        }
                    }

                }
            } catch (GatewayException | TargetException ex) {
                PopupShowDetails.showError("Error while spoofing", ex.getMessage(), true);
            }
        });
        return startSpoofingMenuItem;
    }

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
