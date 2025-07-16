package org.npt.models.ui;

import javafx.beans.property.SimpleStringProperty;

public record SelfDeviceIpEntry(SimpleStringProperty networkInterface, SimpleStringProperty ip,
                                SimpleStringProperty gatewayIp) {

}
