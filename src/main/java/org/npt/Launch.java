package org.npt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.npt.configuration.Configuration;
import org.npt.controllers.View;
import org.npt.services.DataService;
import org.npt.services.DeviceService;
import org.npt.services.impl.DefaultDataService;
import org.npt.services.impl.DeviceServiceImpl;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;

import static org.npt.controllers.View.MAIN_INTERFACE.*;
import static org.npt.controllers.View.*;

public class Launch extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getFxmlResourceAsExternalForm(FXML_FILE));
        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        scene.getStylesheets().add(getCssResourceExternalForm(CSS_FILE));
        stage.setTitle(View.MAIN_INTERFACE.INTERFACE_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        DefaultDataService.init();
        launch();
    }
}