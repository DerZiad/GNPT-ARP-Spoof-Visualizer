package org.npt.controllers.viewdetails;

import java.util.Optional;

import org.npt.controllers.DataInjector;
import org.npt.models.Gateway;
import org.npt.models.IpAddress;
import org.npt.models.SelfDevice;
import org.npt.models.ui.IpEntry;
import org.npt.models.ui.IpEntryWithNetworkInterface;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

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
		SelfDevice selfDevice = (SelfDevice) getArgs()[0];
		Runnable refresh = (Runnable) getArgs()[1];
		deviceNameField.setText(selfDevice.getDeviceName());
		ipColumn.setCellValueFactory(data -> data.getValue().getIp());
		typeColumn.setCellValueFactory(data -> data.getValue().getType());
		networkInterface.setCellValueFactory(data -> data.getValue().getNetworkInterface());
		for (IpAddress ipAddress : selfDevice.getIpAddresses()) {
			IpEntryWithNetworkInterface ipEntryWithNetworkInterface = new IpEntryWithNetworkInterface(new SimpleStringProperty(ipAddress.getIp()),
					new SimpleStringProperty(selfDevice.isValidIPv4(ipAddress.getIp()) ? "IPv4" : "IPv6"),
					new SimpleStringProperty(ipAddress.getNetworkInterface()));
			ipTable.getItems().add(ipEntryWithNetworkInterface);
		}

		ipColumn1.setCellValueFactory(data -> data.getValue().getIp());
		typeColumn1.setCellValueFactory(data -> data.getValue().getType());
		for (Gateway target : selfDevice.getNextGateways()) {
			target.findFirstIPv4().ifPresent(ip -> nextDevicesTable.getItems().add(new IpEntry(ip, selfDevice.isValidIPv4(ip) ? "IPv4" : "IPv6")));
		}
		saveButton.setOnAction(ignored -> {
			String deviceName = deviceNameField.getText();
			selfDevice.setDeviceName(deviceName);
			refresh.run();
		});
	}
}
