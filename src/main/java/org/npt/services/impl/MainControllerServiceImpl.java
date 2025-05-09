package org.npt.services.impl;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.npt.configuration.Configuration;
import org.npt.data.GatewayService;
import org.npt.data.defaults.DefaultGatewayService;
import org.npt.data.defaults.DefaultTargetService;
import org.npt.exception.GatewayException;
import org.npt.exception.TargetException;
import org.npt.exception.children.GatewayIpException;
import org.npt.exception.children.GatewayNotFoundException;
import org.npt.exception.children.TargetIpException;
import org.npt.models.Device;
import org.npt.models.Gateway;
import org.npt.models.Target;
import org.npt.services.ArpSpoofStarter;
import org.npt.data.TargetService;

import java.util.Optional;

public class MainControllerServiceImpl {

    private final ArpSpoofStarter arpSpoofStarter = ArpSpoofStarterImpl.getInstance();

    private final TargetService targetService = new DefaultTargetService();

    private final GatewayService gatewayService = new DefaultGatewayService();

    private void spoof(Target target) throws GatewayNotFoundException, TargetIpException, GatewayIpException {
        Optional<Gateway> gatewayOptional = gatewayService.find().stream().filter(gateway -> gateway.getDevices().contains(target)).findAny();
        Gateway gateway = gatewayOptional.orElseThrow(() -> new GatewayNotFoundException("Couldn't spoof a target that it is not connected"));
        String scanInterface = target.getNetworkInterface();
        String targetIpAddress = target.findFirstIPv4().orElseThrow(()-> new TargetIpException("No IpV4 found for target " + target.getDeviceName()));
        String gatewayIpAddress = gateway.findFirstIPv4().orElseThrow(()-> new GatewayIpException("No IpV4 found for gateway " + gateway.getDeviceName()));
        arpSpoofStarter.startSpoofing(scanInterface,targetIpAddress,gatewayIpAddress);
    }

    private void stopSpoofing(Target target) throws TargetException, GatewayException {
        Optional<Gateway> gatewayOptional = gatewayService.find().stream().filter(gateway -> gateway.getDevices().contains(target)).findAny();
        Gateway gateway = gatewayOptional.orElseThrow(() -> new GatewayNotFoundException("Couldn't spoof a target that it is not connected"));
        String targetIpAddress = target.findFirstIPv4().orElseThrow(()-> new TargetIpException("No IpV4 found for target " + target.getDeviceName()));
        String gatewayIpAddress = gateway.findFirstIPv4().orElseThrow(()-> new GatewayIpException("No IpV4 found for gateway " + gateway.getDeviceName()));
        arpSpoofStarter.stopSpoofing(targetIpAddress,gatewayIpAddress);
    }

    public void initMenu(Device device){
        ContextMenu contextMenu = device.getContextMenu();
        MenuItem detailsItem = new MenuItem("View Details");
        detailsItem.setOnAction(e -> showDeviceDetails(device));

        MenuItem editIpItem = new MenuItem("Edit IP Address");
        editIpItem.setOnAction(e -> editIpAddress(device));

        MenuItem removeItem = new MenuItem("Remove Device");
        //removeItem.setOnAction(e -> targetService);

        if(device instanceof Target) {
            MenuItem startStopSniffing = new MenuItem("Spy");
            startStopSniffing.setOnAction(e -> {
                try {
                    spoof((Target) device);
                } catch (GatewayException | TargetException ex) {
                    PopupShowDetails.showError("Error while spoofing : ",ex.getMessage(),true);
                }
            });
            contextMenu.getItems().add(startStopSniffing);
        }
        contextMenu.getItems().addAll(detailsItem, editIpItem, removeItem);
    }

    private void showDeviceDetails(Device device) {
        PopupShowDetails.popupShowDetailsGatewayOrTarget("details.fxml","My name",true,"text area");
    }

    private void editIpAddress(Device device) {
        System.out.println("Editing IP address of " + device.getDeviceName());
    }

    private void removeDevice(Device device) {
        System.out.println("Removing device " + device.getDeviceName());
    }

    static class PopupShowDetails {

        public static void popupShowDetailsGatewayOrTarget(String fxmlPath, String title, Boolean showAndWait, String text) {
            try {
                FXMLLoader loader = new FXMLLoader(PopupShowDetails.class.getResource(fxmlPath));
                Parent root = loader.load();
                Stage popupStage = new Stage();
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.setTitle(title);
                popupStage.setScene(new Scene(root));
                if(showAndWait) popupStage.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public static void showError(String title, String message, Boolean showAndWait) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("An error occurred");
            alert.setContentText(message);
            if(showAndWait)
                alert.showAndWait();
        }
    }
}
