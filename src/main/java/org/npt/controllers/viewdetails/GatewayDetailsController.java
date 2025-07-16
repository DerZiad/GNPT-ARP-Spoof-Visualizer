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
    public TextField deviceNameField;

    @FXML
    private TableView<IpEntry> ipTable;

    @FXML
    private TableColumn<IpEntry, String> ipColumn;

    @FXML
    private TableColumn<IpEntry, String> typeColumn;

    @FXML
    public TableView<IpEntry> nextDevicesTable;

    @FXML
    private TableColumn<IpEntry, String> ipColumn1;

    @FXML
    private TableColumn<IpEntry, String> typeColumn1;

    @FXML
    public Button saveButton;

    @FXML
    public void initialize() {
        Gateway gateway = (Gateway) super.getArgs()[0];
        Runnable refresh = (Runnable) super.getArgs()[1];
        deviceNameField.setText(gateway.getDeviceName());
        ipColumn.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn.setCellValueFactory(data -> data.getValue().getType());
        ipTable.getItems().add(new IpEntry(gateway.getIp(), "IPv4"));

        ipColumn1.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn1.setCellValueFactory(data -> data.getValue().getType());
        for (Target target : gateway.getDevices()) {
            final IpEntry ipEntry = new IpEntry(target.getIp(), "IPv4");
            nextDevicesTable.getItems().add(ipEntry);
        }
        saveButton.setOnAction(ignored -> {
            String deviceName = deviceNameField.getText();
            gateway.setDeviceName(deviceName);
            refresh.run();
        });
    }
}
