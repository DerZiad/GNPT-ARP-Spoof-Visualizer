package org.npt;

import javafx.application.Application;
import javafx.stage.Stage;
import org.npt.uiservices.FrameService;
import org.npt.services.DataService;
import org.npt.services.defaults.DefaultDataService;

import java.io.IOException;

public class Launch extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            DataService dataService = DefaultDataService.getInstance();
            dataService.run();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start data service", e);
        }
        FrameService frameService = FrameService.getInstance();
        frameService.runMainFrame(stage);
    }

    public static void main(String[] args) {
        launch(args);
        System.exit(0);
    }
}
