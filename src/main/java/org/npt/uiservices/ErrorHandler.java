package org.npt.uiservices;

import javafx.scene.control.Alert;
import org.npt.exception.InvalidInputException;
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

    public static void handle(InvalidInputException e) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(ERROR_TITLE);
        alert.setHeaderText("Invalid Input Exception");
        StringBuilder errorMessage = new StringBuilder(e.getMessage() + " : \n");
        for (String key : e.getErrors().keySet()) {
            errorMessage.append("* ")
                    .append(key)
                    .append(" - ")
                    .append(e.getErrors().get(key))
                    .append("\n");
        }
        alert.setContentText(errorMessage.toString());
        alert.show();
    }
}
