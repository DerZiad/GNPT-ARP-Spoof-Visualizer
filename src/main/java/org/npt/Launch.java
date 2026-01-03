package org.npt;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.npt.exception.DrawNetworkException;
import org.npt.exception.ShutdownException;
import org.npt.services.DataService;
import org.npt.services.RequirementVerifier;
import org.npt.services.defaults.DefaultDataService;
import org.npt.uiservices.FrameService;

import java.io.IOException;

@Slf4j
public class Launch extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            final DataService dataService = DefaultDataService.getInstance();
            dataService.run();
        } catch (DrawNetworkException e) {
            log.error(e.getMessage());
        }
        FrameService frameService = FrameService.getInstance();
        frameService.runMainFrame(stage);
    }

    public static void main(String[] args) {
        try {
            RequirementVerifier.validate();
        }catch (ShutdownException e){
            log.error(e.getMessage());
            System.exit(e.getCode());
        }

        launch(args);
        System.exit(0);
    }
}
