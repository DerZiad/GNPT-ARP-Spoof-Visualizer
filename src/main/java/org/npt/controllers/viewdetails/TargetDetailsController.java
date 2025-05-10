package org.npt.controllers.viewdetails;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.npt.models.Target;

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
    private TableView<IpEntry> ipTable;

    private Target target;

    public void setData(Target target) {
        this.target = target;
        deviceNameField.setText(target.getDeviceName());
        interfaceField.setText(target.getNetworkInterface());
        ipTable.getItems().add(new IpEntry(target.getNetworkInterface(),target.getIpAddresses().getFirst()));
    }
}
