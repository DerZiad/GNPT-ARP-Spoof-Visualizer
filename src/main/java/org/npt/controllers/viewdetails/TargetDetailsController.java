package org.npt.controllers.viewdetails;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.npt.controllers.DataInjector;
import org.npt.models.Target;

public class TargetDetailsController extends DataInjector {

    @FXML
    public TextField deviceNameTextField;

    @FXML
    public TextField ipTextField;

    @FXML
    public Button saveButton;

    @FXML
    public void initialize() {
        Target target = (Target) getArgs()[0];
        Runnable refresh = (Runnable) getArgs()[1];
        fillTableData(target);
        saveButton.setOnAction(ignored -> {
            target.setDeviceName(deviceNameTextField.getText());
            target.setIp(ipTextField.getText());
            refresh.run();
        });
    }

    private void fillTableData(Target target) {
        deviceNameTextField.setText(target.getDeviceName());
        ipTextField.setText(target.getIp());
    }
}
