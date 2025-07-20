package org.npt.uiservices;

import javafx.scene.control.Alert;
import org.npt.exception.NotFoundException;

public class ErrorHandler {

    private static final String ERROR_TITLE = "Error Occurred while processing your request";

    public static void handle(NotFoundException e) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(ERROR_TITLE);
        alert.setHeaderText("Not Found Exception");
        alert.setContentText(e.getMessage());
        alert.show();
    }
}
