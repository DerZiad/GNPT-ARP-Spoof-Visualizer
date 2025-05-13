package org.npt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Setter;
import org.npt.controllers.StatisticsController;
import org.npt.data.DataService;
import org.npt.data.defaults.DefaultDataService;
import org.npt.models.Target;

import java.io.IOException;

import static org.npt.controllers.View.MAIN_INTERFACE.*;
import static org.npt.controllers.View.getCssResourceExternalForm;
import static org.npt.controllers.View.getFxmlResourceAsExternalForm;

public class Launch extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getFxmlResourceAsExternalForm(FXML_FILE));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.getStylesheets().add(getCssResourceExternalForm(CSS_FILE));
        stage.setTitle(INTERFACE_TITLE);
        stage.setScene(scene);
        stage.show();

        StageSwitcher.setPrimaryStage(stage);
    }

    public static void main(String[] args) {
        try {
            DataService dataService = DefaultDataService.getInstance();
            dataService.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        boolean isHeadless = Boolean.getBoolean("headless");
        if (!isHeadless) {
            launch();
        }
    }

    public static class StageSwitcher {

        @Setter
        private static Stage primaryStage;

        public static void switchTo(String fxmlFile, int width, int height, String title, Target target) {
            try {
                FXMLLoader loader = new FXMLLoader(getFxmlResourceAsExternalForm(fxmlFile));
                Parent root = loader.load();
                StatisticsController controller = loader.getController();
                controller.setData(target);
                Scene newScene = new Scene(root, width, height);
                primaryStage.setTitle(title);
                primaryStage.setScene(newScene);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
