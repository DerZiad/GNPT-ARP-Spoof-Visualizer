package org.npt.controllers;

import org.npt.Launch;

import java.net.URL;
import java.util.Objects;

public class View {

    public static final String CSS_PATH = "/org/npt/style/";

    public static class MAIN_INTERFACE {

        public static final String INTERFACE_TITLE = "NetworkPacketTracer";

        public static final String FXML_FILE = "main_frame.fxml";

        public static final String CSS_FILE = "main_frame.css";

        public static final Integer WIDTH = 1054;

        public static final Integer HEIGHT = 674;
    }

    public static class TARGET_DETAILS_VIEW {

        public static final String INTERFACE_TITLE = "Target Details";

        public static final String FXML_FILE = "target-details.fxml";

        public static final Integer WIDTH = 722;

        public static final Integer HEIGHT = 640;
    }

    public static class GATEWAY_DETAILS_VIEW {

        public static final String INTERFACE_TITLE = "Gateway Details";

        public static final String FXML_FILE = "gateway-details.fxml";

        public static final Integer WIDTH = 722;

        public static final Integer HEIGHT = 800;
    }

    public static class SELF_DEVICE_DETAILS_VIEW {

        public static final String INTERFACE_TITLE = "Self Device Details";

        public static final String FXML_FILE = "selfdevice-details.fxml";

        public static final Integer WIDTH = 730;

        public static final Integer HEIGHT = 630;
    }

    public static class STATISTICS_DETAILS_VIEW {

        public static final String INTERFACE_TITLE = "Statistics";

        public static final String FXML_FILE = "show-statistics.fxml";

        public static final Integer WIDTH = 1054;

        public static final Integer HEIGHT = 674;
    }

    public static String getCssPath(String fileName) {
        return CSS_PATH + fileName;
    }

    public static String getCssResourceExternalForm(String fileName) {
        return Objects.requireNonNull(Launch.class.getResource(View.getCssPath(fileName))).toExternalForm();
    }

    public static URL getFxmlResourceAsExternalForm(String fileName) {
        return Objects.requireNonNull(Launch.class.getResource(fileName));
    }

}