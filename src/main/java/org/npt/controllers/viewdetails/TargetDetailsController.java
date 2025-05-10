package org.npt.controllers.viewdetails;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.npt.models.Target;

import java.util.function.Consumer;

@AllArgsConstructor
@Getter
class IpEntry {
    private final SimpleStringProperty ip;
    private final SimpleStringProperty networkInterface;

    IpEntry(String networkInterface, String ip){
        this.ip = new SimpleStringProperty(ip);
        this.networkInterface = new SimpleStringProperty(networkInterface);
    }
}


public class TargetDetailsController {

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

    public void setData(Target target, Consumer<Void> refresh) {
        deviceNameField.setText(target.getDeviceName());
        interfaceField.setText(target.getNetworkInterface());
        ipColumn.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn.setCellValueFactory(data -> data.getValue().getNetworkInterface());
        for(String ip:target.getIpAddresses()){
            ipTable.getItems().add(new IpEntry(target.isValidIPv4(ip)?"IPv4":"IPv6",ip));
        }
        saveButton.setOnAction(_ -> {
            String deviceName = deviceNameField.getText();
            String networkInterface = interfaceField.getText();
            target.setNetworkInterface(networkInterface);
            target.setDeviceName(deviceName);
            refresh.accept(null);
        });
    }
}
