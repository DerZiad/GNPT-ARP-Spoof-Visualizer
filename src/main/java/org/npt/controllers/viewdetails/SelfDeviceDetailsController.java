package org.npt.controllers.viewdetails;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import lombok.Getter;
import org.npt.models.Gateway;
import org.npt.models.IpAddress;
import org.npt.models.SelfDevice;
import org.npt.models.Target;

import java.util.function.Consumer;

@Getter
class IpEntryWithNetworkInterface extends IpEntry {

    private final SimpleStringProperty networkInterface;

    public IpEntryWithNetworkInterface(SimpleStringProperty ip, SimpleStringProperty type, SimpleStringProperty networkInterface) {
        super(ip, type);
        this.networkInterface = networkInterface;
    }
}

public class SelfDeviceDetailsController {

    @FXML
    public TextField deviceNameField;

    @FXML
    private TableView<IpEntry> ipTable;

    @FXML
    private TableColumn<IpEntryWithNetworkInterface, String> ipColumn;

    @FXML
    private TableColumn<IpEntryWithNetworkInterface, String> typeColumn;

    @FXML
    public TableColumn<IpEntryWithNetworkInterface, String> networkInterface;

    @FXML
    public TableView<IpEntry> nextDevicesTable;

    @FXML
    private TableColumn<IpEntry, String> ipColumn1;

    @FXML
    private TableColumn<IpEntry, String> typeColumn1;

    @FXML
    public Button saveButton;


    public void setData(SelfDevice selfDevice, Consumer<Void> refresh) {
        deviceNameField.setText(selfDevice.getDeviceName());
        ipColumn.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn.setCellValueFactory(data -> data.getValue().getType());
        networkInterface.setCellValueFactory(data -> data.getValue().getNetworkInterface());
        for(IpAddress ipAddress:selfDevice.getIpAddresses()){
            IpEntryWithNetworkInterface ipEntryWithNetworkInterface = new IpEntryWithNetworkInterface(new SimpleStringProperty(ipAddress.getIp()),
                    new SimpleStringProperty(selfDevice.isValidIPv4(ipAddress.getIp())?"IPv4":"IPv6"),
                    new SimpleStringProperty(ipAddress.getNetworkInterface()));
            ipTable.getItems().add(ipEntryWithNetworkInterface);
        }

        ipColumn1.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn1.setCellValueFactory(data -> data.getValue().getType());
        for (Gateway target:selfDevice.getNextGateways()){
            String ip = target.findFirstIPv4().get();
            nextDevicesTable.getItems().add(new IpEntry(ip, selfDevice.isValidIPv4(ip)?"IPv4":"IPv6"));
        }
        saveButton.setOnAction(_ -> {
            String deviceName = deviceNameField.getText();
            selfDevice.setDeviceName(deviceName);
            refresh.accept(null);
        });
    }
}
