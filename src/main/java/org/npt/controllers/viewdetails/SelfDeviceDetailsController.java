package org.npt.controllers.viewdetails;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.npt.controllers.DataInjector;
import org.npt.models.Interface;
import org.npt.models.SelfDevice;
import org.npt.models.ui.SelfDeviceIpEntry;
import org.npt.services.GraphicalNetworkTracerFactory;

public class SelfDeviceDetailsController extends DataInjector {

    @FXML
    public TextField deviceNameField;

    @FXML
    public TableView<SelfDeviceIpEntry> ipTable;

    @FXML
    public TableColumn<SelfDeviceIpEntry, String> networkInterface;

    @FXML
    public TableColumn<SelfDeviceIpEntry, String> ipColumn;

    @FXML
    public TableColumn<SelfDeviceIpEntry, String> gatewayIp;

    @FXML
    public Button saveButton;


    @FXML
    public void initialize() {
        final SelfDevice selfDevice = GraphicalNetworkTracerFactory.getInstance().getDataService().getSelfDevice();
        final Runnable refresh = (Runnable) getArgs()[0];
        deviceNameField.setText(selfDevice.getDeviceName());
        networkInterface.setCellValueFactory(data -> data.getValue().networkInterface());
        ipColumn.setCellValueFactory(data -> data.getValue().ip());
        gatewayIp.setCellValueFactory(data -> data.getValue().gatewayIp());
        for (Interface anInterface : selfDevice.getAnInterfaces()) {
            final SelfDeviceIpEntry selfDeviceIpEntry = new SelfDeviceIpEntry(new SimpleStringProperty(anInterface.getIp()),
                    new SimpleStringProperty(anInterface.getIp()),
                    anInterface.getGatewayOptional().isPresent() ?
                            new SimpleStringProperty(anInterface.getGatewayOptional().get().getIp()) :
                            new SimpleStringProperty("UNKNOWN"));
            ipTable.getItems().add(selfDeviceIpEntry);
        }

        saveButton.setOnAction(ignored -> {
            selfDevice.setDeviceName(deviceNameField.getText());
            refresh.run();
        });
    }
}
