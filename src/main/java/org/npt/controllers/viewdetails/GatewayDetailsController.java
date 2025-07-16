package org.npt.controllers.viewdetails;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.npt.controllers.DataInjector;
import org.npt.models.Gateway;
import org.npt.models.Target;
import org.npt.models.ui.IpEntry;

public class GatewayDetailsController extends DataInjector {

    @FXML
    public TextField deviceNameTextField;

    @FXML
    public TextField ipTextField;

    @FXML
    public TableColumn<IpEntry, String> ipColumn;

    @FXML
    public TableColumn<IpEntry, String> typeColumn;

    @FXML
    public TableView<IpEntry> nextDevicesTable;

    @FXML
    public Button saveButton;

    @FXML
    public void initialize() {
        Gateway gateway = (Gateway) super.getArgs()[0];
        Runnable refresh = (Runnable) super.getArgs()[1];
        deviceNameTextField.setText(gateway.getDeviceName());
        ipTextField.setText(gateway.getIp());
        ipColumn.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn.setCellValueFactory(data -> data.getValue().getType());

        for (Target target : gateway.getDevices()) {
            final IpEntry ipEntry = new IpEntry(target.getIp(), "IPv4");
            nextDevicesTable.getItems().add(ipEntry);
        }
        saveButton.setOnAction(ignored -> {
            String deviceName = deviceNameTextField.getText();
            gateway.setDeviceName(deviceName);
            refresh.run();
        });
    }
}
