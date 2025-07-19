package org.npt;

import javafx.application.Application;
import javafx.stage.Stage;
import org.npt.exception.DrawNetworkException;
import org.npt.exception.ShutdownException;
import org.npt.services.DataService;
import org.npt.services.defaults.DefaultDataService;
import org.npt.uiservices.FrameService;

import java.io.IOException;

public class Launch extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            check();
            final DataService dataService = DefaultDataService.getInstance();
            dataService.run();
        } catch (DrawNetworkException e) {
            System.err.println(e.getMessage());
        } catch (ShutdownException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        FrameService frameService = FrameService.getInstance();
        frameService.runMainFrame(stage);
    }

    public static void main(String[] args) {
        launch(args);
        System.exit(0);
    }

    private static void check() throws ShutdownException {

        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("linux")) {
            throw new ShutdownException(
                    String.format(ShutdownException.ERROR_FORMAT, "OS Check", "System is not Linux"),
                    ShutdownException.ShutdownExceptionErrorCode.FAILED_TO_LOAD_PROPERTY_FILE
            );
        }

        String user = System.getProperty("user.name");
        if (!"root".equals(user)) {
            throw new ShutdownException(
                    String.format(ShutdownException.ERROR_FORMAT, "Permission Check", "Not running as sudo/root"),
                    ShutdownException.ShutdownExceptionErrorCode.FAILED_TO_LOAD_PROPERTY_FILE
            );
        }

        try {
            Process process = new ProcessBuilder("which", "nmap").start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ShutdownException(
                        String.format(ShutdownException.ERROR_FORMAT, "Dependency Check", "nmap not found"),
                        ShutdownException.ShutdownExceptionErrorCode.FAILED_TO_LOAD_PROPERTY_FILE
                );
            }
        } catch (IOException | InterruptedException e) {
            throw new ShutdownException(
                    String.format(ShutdownException.ERROR_FORMAT, "Dependency Check", "Error checking nmap"),
                    ShutdownException.ShutdownExceptionErrorCode.FAILED_TO_LOAD_PROPERTY_FILE
            );
        }
    }
}
