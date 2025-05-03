package org.npt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.npt.configuration.Configuration;
import org.npt.services.DeviceService;
import org.npt.services.impl.DeviceServiceImpl;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
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
        DeviceService deviceService = new DeviceServiceImpl();
        Configuration.gateways = deviceService.scanCurrentGateways();
        Configuration.selfDevice = deviceService.scanActualDevice(Configuration.gateways);
        launch();
    }
}