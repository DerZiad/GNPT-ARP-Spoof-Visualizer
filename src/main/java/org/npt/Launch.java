package org.npt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.npt.controllers.View;
import org.npt.data.DataService;
import org.npt.data.defaults.DefaultDataService;
import org.npt.services.impl.ProcessService;

import java.io.IOException;

import static org.npt.controllers.View.MAIN_INTERFACE.*;
import static org.npt.controllers.View.getCssResourceExternalForm;
import static org.npt.controllers.View.getFxmlResourceAsExternalForm;

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

    public static void main(String[] args) {
        try {
            DataService dataService = DefaultDataService.getInstance();
            dataService.run();
            ProcessService.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        boolean isHeadless = Boolean.getBoolean("headless");
        if (!isHeadless) {
            launch();
        }
    }
}