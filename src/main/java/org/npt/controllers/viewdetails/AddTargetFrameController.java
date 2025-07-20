package org.npt.controllers.viewdetails;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.npt.controllers.DataInjector;
import org.npt.exception.InvalidInputException;
import org.npt.models.Interface;
import org.npt.uiservices.DeviceUiMapperService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTargetFrameController extends DataInjector {

    private DeviceUiMapperService deviceUiMapperService;

    @FXML
    public TextField ipTextField;
    @FXML
    public TextField deviceNameTextField;
    @FXML
    public MenuButton menuButton;

    @FXML
    public VBox errorAlertBox;

    @FXML
    public Label errorTitle;

    @FXML
    public VBox errorMessages;

    @FXML
    public VBox successAlertBox;

    @FXML
    public Label successTitle;

    @FXML
    public VBox successMessages;

    @FXML
    public Button saveButton;

    @FXML
    public void initialize() {
        deviceUiMapperService = (DeviceUiMapperService) super.getArgs()[0];
        hideAlerts();
        initFormFieldInterfacesComboBox();
        saveButton.setOnAction(e -> addTarget());
    }

    @FXML
    private void addTarget() {
        final String ip = ipTextField.getText();
        final String anInterface = menuButton.getText();
        final String name = deviceNameTextField.getText();

        hideAlerts();

        try {
            deviceUiMapperService.addTarget(ip, anInterface, name);
            showSuccess();
            clearInputs();
        } catch (InvalidInputException e) {
            errorTitle.setText("⚠ " + e.getMessage());
            showErrors(e.getErrors());
        }
    }

    private void clearInputs() {
        ipTextField.setText("");
        deviceNameTextField.setText("");
    }

    private void hideAlerts() {
        errorAlertBox.setVisible(false);
        errorAlertBox.setManaged(false);
        successAlertBox.setVisible(false);
        successAlertBox.setManaged(false);
    }

    private void showErrors(HashMap<String, String> errors) {
        errorAlertBox.setVisible(true);
        errorAlertBox.setManaged(true);
        errorMessages.getChildren().clear();

        for (Map.Entry<String, String> entry : errors.entrySet()) {
            Label label = new Label("• " + entry.getKey() + ": " + entry.getValue());
            label.getStyleClass().add("alert-item");
            errorMessages.getChildren().add(label);
        }
    }

    private void showSuccess() {
        successAlertBox.setVisible(true);
        successAlertBox.setManaged(true);
        successTitle.setText("✔ " + "Success: Target Added");
        successMessages.getChildren().clear();

        Label label = new Label("The device was added successfully.");
        label.getStyleClass().add("alert-item");
        successMessages.getChildren().add(label);
    }

    private void initFormFieldInterfacesComboBox() {
        final List<Interface> interfaces = deviceUiMapperService.getSelfDevice().getAnInterfaces();
        menuButton.getItems().clear();
        for (Interface interfaceDevice : interfaces) {
            final MenuItem menuItem = new MenuItem(interfaceDevice.getDeviceName());
            menuItem.setOnAction(ignored -> menuButton.setText(menuItem.getText()));
            menuButton.getItems().add(menuItem);
        }

        if (menuButton.getItems().isEmpty()) {
            menuButton.setText("No network");
        } else {
            menuButton.setText(menuButton.getItems().getFirst().getText());
        }
    }
}
