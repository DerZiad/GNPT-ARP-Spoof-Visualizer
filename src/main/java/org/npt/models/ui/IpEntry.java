package org.npt.models.ui;

import javafx.beans.property.SimpleStringProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IpEntry {

    private final SimpleStringProperty ip;
    private final SimpleStringProperty type;

    public IpEntry(String ip, String type) {
        this.ip = new SimpleStringProperty(ip);
        this.type = new SimpleStringProperty(type);
    }
}
