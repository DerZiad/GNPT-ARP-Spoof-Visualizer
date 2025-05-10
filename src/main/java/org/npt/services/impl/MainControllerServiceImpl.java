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
import org.npt.controllers.viewdetails.TargetDetailsController;
import org.npt.controllers.View;
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
import org.npt.networkservices.defaults.ArpSpoofStarterImpl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static org.npt.controllers.View.getFxmlResourceAsExternalForm;

public class MainControllerServiceImpl {

    private final ArpSpoofStarter arpSpoofStarter = ArpSpoofStarterImpl.getInstance();

    private final DataService dataService = DefaultDataService.getInstance();

    private final GatewayService gatewayService = new DefaultGatewayService();

    @Getter
    private final Set<Device> devices = new HashSet<>();

    private void spoof(Target target) throws GatewayNotFoundException, TargetIpException, GatewayIpException {
        Optional<Gateway> gatewayOptional = gatewayService.find().stream().filter(gateway -> gateway.getDevices().contains(target)).findAny();
        Gateway gateway = gatewayOptional.orElseThrow(() -> new GatewayNotFoundException("Couldn't spoof a target that it is not connected"));
        String scanInterface = target.getNetworkInterface();
        String targetIpAddress = target.findFirstIPv4().orElseThrow(() -> new TargetIpException("No IpV4 found for target " + target.getDeviceName()));
        String gatewayIpAddress = gateway.findFirstIPv4().orElseThrow(() -> new GatewayIpException("No IpV4 found for gateway " + gateway.getDeviceName()));
        arpSpoofStarter.startSpoofing(scanInterface, targetIpAddress, gatewayIpAddress);
    }

    private void stopSpoofing(Target target) throws TargetException, GatewayException {
        Optional<Gateway> gatewayOptional = gatewayService.find().stream().filter(gateway -> gateway.getDevices().contains(target)).findAny();
        Gateway gateway = gatewayOptional.orElseThrow(() -> new GatewayNotFoundException("Couldn't spoof a target that it is not connected"));
        String targetIpAddress = target.findFirstIPv4().orElseThrow(() -> new TargetIpException("No IpV4 found for target " + target.getDeviceName()));
        String gatewayIpAddress = gateway.findFirstIPv4().orElseThrow(() -> new GatewayIpException("No IpV4 found for gateway " + gateway.getDeviceName()));
        arpSpoofStarter.stopSpoofing(targetIpAddress, gatewayIpAddress);
    }

    public void initMenu(Device device, Consumer<Void> refresh) {
        if (device instanceof SelfDevice)
            return;

        ContextMenu contextMenu = device.getContextMenu();
        MenuItem detailsItem = new MenuItem("View Details");
        detailsItem.setOnAction(_ -> PopupShowDetails.popupShowDetailsGatewayOrTarget((Target) device, refresh));

        MenuItem removeItem = new MenuItem("Remove Device");
        removeItem.setOnAction(_ -> {
            dataService.removeByObject(Optional.of(device));
            refresh.accept(null);
        });

        if (device instanceof Target) {
            MenuItem startStopSniffing = new MenuItem("Start Spoofing");
            startStopSniffing.setOnAction(e -> {
                try {
                    spoof((Target) device);
                    devices.add(device);
                    refresh.accept(null);
                } catch (GatewayException | TargetException ex) {
                    PopupShowDetails.showError("Error while spoofing", ex.getMessage(), true);
                }
            });
            contextMenu.getItems().add(startStopSniffing);
        }
        contextMenu.getItems().addAll(detailsItem, removeItem);
    }

    public static class PopupShowDetails {

        public static void popupShowDetailsGatewayOrTarget(Target target, Consumer<Void> refresh) {
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


        public static void showError(String title, String message, Boolean showAndWait) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("An error occurred");
            alert.setHeaderText(title);
            alert.setContentText(message);
            if (showAndWait)
                alert.showAndWait();
        }
    }
}
