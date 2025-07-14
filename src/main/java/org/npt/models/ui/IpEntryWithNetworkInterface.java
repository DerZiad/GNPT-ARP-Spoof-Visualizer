package org.npt.models.ui;

import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

@Getter
public class IpEntryWithNetworkInterface extends IpEntry {

	private final SimpleStringProperty networkInterface;

	public IpEntryWithNetworkInterface(SimpleStringProperty ip, SimpleStringProperty type, SimpleStringProperty networkInterface) {
		super(ip, type);
		this.networkInterface = networkInterface;
	}
}
