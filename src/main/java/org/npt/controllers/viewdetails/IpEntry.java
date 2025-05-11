package org.npt.controllers.viewdetails;

import javafx.beans.property.SimpleStringProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IpEntry {

    private final SimpleStringProperty ip;
    private final SimpleStringProperty type;

    IpEntry(String ip, String type){
        this.ip = new SimpleStringProperty(ip);
        this.type = new SimpleStringProperty(type);
    }
}
