package org.npt.controllers.viewdetails;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.npt.models.Gateway;
import org.npt.models.Target;

public class GatewayDetailsController {

    @FXML
    public TextField deviceNameField;

    @FXML
    public TextField interfaceField;

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


    public void setData(Gateway gateway, Runnable refresh) {
        deviceNameField.setText(gateway.getDeviceName());
        interfaceField.setText(gateway.getNetworkInterface());
        ipColumn.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn.setCellValueFactory(data -> data.getValue().getType());
        for(String ip:gateway.getIpAddresses()){
            ipTable.getItems().add(new IpEntry(ip, gateway.isValidIPv4(ip)?"IPv4":"IPv6"));
        }

        ipColumn1.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn1.setCellValueFactory(data -> data.getValue().getType());
        for (Target target:gateway.getDevices()){
            String ip = target.findFirstIPv4().get();
            nextDevicesTable.getItems().add(new IpEntry(ip, gateway.isValidIPv4(ip)?"IPv4":"IPv6"));
        }
        saveButton.setOnAction(ignored -> {
            String deviceName = deviceNameField.getText();
            String networkInterface = interfaceField.getText();
            gateway.setNetworkInterface(networkInterface);
            gateway.setDeviceName(deviceName);
            refresh.run();
        });
    }
}
