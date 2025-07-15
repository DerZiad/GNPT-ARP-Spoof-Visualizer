package org.npt.controllers.viewdetails;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.npt.controllers.DataInjector;
import org.npt.models.Target;
import org.npt.models.ui.IpEntry;

public class TargetDetailsController extends DataInjector {

    @FXML
    public TextField deviceNameField;

    @FXML
    public TextField interfaceField;

    @FXML
    public Button saveButton;

    @FXML
    private TableView<IpEntry> ipTable;

    @FXML
    private TableColumn<IpEntry, String> ipColumn;

    @FXML
    private TableColumn<IpEntry, String> typeColumn;

    @FXML
    public void initialize() {
        Target target = (Target) getArgs()[0];
        Runnable refresh = (Runnable) getArgs()[1];
        fillTableData(target);
        saveButton.setOnAction(ignored -> {
            String deviceName = deviceNameField.getText();
            String networkInterface = interfaceField.getText();
            target.setNetworkInterface(networkInterface);
            target.setDeviceName(deviceName);
            refresh.run();
        });
    }

    private void fillTableData(Target target) {
        deviceNameField.setText(target.getDeviceName());
        interfaceField.setText(target.getNetworkInterface());
        ipColumn.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn.setCellValueFactory(data -> data.getValue().getType());
        for (String ip : target.getIpAddresses()) {
            ipTable.getItems().add(new IpEntry(ip, target.isValidIPv4(ip) ? "IPv4" : "IPv6"));
        }
    }
}
