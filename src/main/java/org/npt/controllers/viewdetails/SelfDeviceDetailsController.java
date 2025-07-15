package org.npt.controllers.viewdetails;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.npt.controllers.DataInjector;
import org.npt.models.Gateway;
import org.npt.models.Interface;
import org.npt.models.SelfDevice;
import org.npt.models.ui.IpEntry;
import org.npt.models.ui.IpEntryWithNetworkInterface;
import org.npt.services.GraphicalNetworkTracerFactory;

public class SelfDeviceDetailsController extends DataInjector {

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


    @FXML
    public void initialize() {
        SelfDevice selfDevice = GraphicalNetworkTracerFactory.getInstance().getDataService().getSelfDevice();
        Runnable refresh = (Runnable) getArgs()[0];
        deviceNameField.setText(selfDevice.getDeviceName());
        ipColumn.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn.setCellValueFactory(data -> data.getValue().getType());
        ipColumn1.setCellValueFactory(data -> data.getValue().getIp());
        typeColumn1.setCellValueFactory(data -> data.getValue().getType());
        networkInterface.setCellValueFactory(data -> data.getValue().getNetworkInterface());
        for (Interface anInterface : selfDevice.getAnInterfaces()) {
            IpEntryWithNetworkInterface ipEntryWithNetworkInterface = new IpEntryWithNetworkInterface(new SimpleStringProperty(anInterface.getIp()),
                    new SimpleStringProperty("IPv4"),
                    new SimpleStringProperty(anInterface.getDeviceName()));
            ipTable.getItems().add(ipEntryWithNetworkInterface);
            final Gateway gateway = anInterface.getGateway();
            nextDevicesTable.getItems().add(new IpEntry(gateway.getIp(), "IPv4"));
        }

        saveButton.setOnAction(ignored -> {
            String deviceName = deviceNameField.getText();
            selfDevice.setDeviceName(deviceName);
            refresh.run();
        });
    }
}
