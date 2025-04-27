package org.npt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
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

    public static void main(String[] args) {
        launch();
    }
}