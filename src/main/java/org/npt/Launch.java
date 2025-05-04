package org.npt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.stage.Stage;
import org.npt.beans.implementation.DeviceService;
import org.npt.configuration.Configuration;
import org.npt.models.IpAddress;
import org.npt.models.Target;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Launch extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launch.class.getResource("Interface.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1314, 699);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/org/npt/style/mainInterface.css")).toExternalForm());
        stage.setTitle("NetworkPacketTracer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        DeviceService deviceService = new DeviceService();
        Configuration.gateways = deviceService.scanCurrentGateways();
        Configuration.selfDevice = deviceService.scanActualDevice(Configuration.gateways);
        Configuration.targets = new ArrayList<>();
        Configuration.targets.add(new Target("TEST",List.of(new IpAddress("dsfds","qs")),0,0,new ContextMenu()));
        Configuration.targets.add(new Target("TEST",List.of(new IpAddress("dsfds","qs")),0,0,new ContextMenu()));
        Configuration.gateways.getFirst().getDevices().addAll(Configuration.targets);
        launch();
    }
}