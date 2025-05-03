module org.npt {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires static lombok;
    requires org.pcap4j.core;
    requires org.dnsjava;
    requires annotations;

    opens org.npt to javafx.fxml;
    exports org.npt;
    opens org.npt.controllers;
    exports org.npt.controllers;
    exports org.npt.models;
    opens org.npt.models;
    exports org.npt.services;
    opens org.npt.services;
    exports org.npt.services.impl;
    opens org.npt.services.impl;
}