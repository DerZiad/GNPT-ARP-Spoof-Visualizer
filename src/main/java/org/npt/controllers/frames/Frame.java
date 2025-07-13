package org.npt.controllers.frames;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.npt.controllers.MainController;

@AllArgsConstructor
@Data
public class Frame {

    private String key;
    private String title;
    private String fxmlLocation;
    private Class<?> controllerClass;
    private Object[] args;
    private Size size;

    public record Size(double width, double height) {

    }

    public static Frame createMainFrame() {
        return new Frame(
                "mainFrame",
                "Graphical Network Tracer",
                "fxml/main_frame.fxml",
                MainController.class,
                new Object[]{},
                new Size(930.0,593.0)
        );
    }
}