package org.npt.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import org.npt.exception.ShutdownException;
import org.npt.models.DefaultPacket;
import org.npt.models.KnownHost;
import org.npt.models.Target;
import org.npt.models.ui.IncomingOutgoingPacket;
import org.npt.services.ArpSpoofService;
import org.npt.services.GraphicalNetworkTracerFactory;
import org.npt.services.defaults.DefaultArpSpoofService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class StatisticsController extends DataInjector {

    private static final Integer MAX_ELEMENT_TO_DISPLAY_PER_PANE = 10;

    @FXML
    private VBox vboxPane2;

    @FXML
    private VBox vboxPane1;

    @FXML
    private VBox vboxPane;

    @Getter
    private Target target;

    private DefaultArpSpoofService.DeviceSniffer deviceSniffer;
    private HashMap<String, KnownHost> knownHosts;

    private GraphicalNetworkTracerFactory graphicalNetworkTracerFactory;

    @FXML
    public void initialize() {
        graphicalNetworkTracerFactory = GraphicalNetworkTracerFactory.getInstance();
        knownHosts = graphicalNetworkTracerFactory.getKnownHosts();

        // Initialize target from injected args, make sure it's saved to this.target
        this.target = (Target) super.getArgs()[0];

        deviceSniffer = findDeviceSniffer(target);
        if (deviceSniffer == null) {
            System.err.println("DeviceSniffer not found for target: " + target);
            return;
        } else {
            deviceSniffer = findDeviceSniffer(target);
        }

        startRepeatingUpdates();
    }

    public DefaultArpSpoofService.DeviceSniffer findDeviceSniffer(Target target) {
        ArpSpoofService arpSpoofService = GraphicalNetworkTracerFactory.getInstance().getArpSpoofService();
        Optional<DefaultArpSpoofService.ArpSpoofProcess> arpSpoofProcessOpt = arpSpoofService.getArpSpoofProcess(target);
        return Objects.requireNonNull(arpSpoofProcessOpt.map(DefaultArpSpoofService.ArpSpoofProcess::packetSnifferThreadPair).orElse(null)).getValue();
    }

    private void startRepeatingUpdates() {
        Timeline refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> {
                    try {
                        updateStatistics();
                    } catch (ShutdownException ex) {
                        ex.printStackTrace();
                    }
                }),
                new KeyFrame(Duration.seconds(2))
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void updateStatistics() throws ShutdownException {
        // UI updates MUST run on JavaFX Application Thread:
        Platform.runLater(() -> {
            vboxPane.getChildren().clear();
            vboxPane1.getChildren().clear();
            vboxPane2.getChildren().clear();

            Map<String, IncomingOutgoingPacket> data = calculateNumberOfPackets();

            int counter = 0;
            int column = 0;
            for (String key : knownHosts.keySet()) {
                IncomingOutgoingPacket incomingOutgoingPacket = data.get(key);
                if (incomingOutgoingPacket != null) {
                    addToPane(incomingOutgoingPacket, column);

                    counter++;
                    if (counter >= MAX_ELEMENT_TO_DISPLAY_PER_PANE) {
                        column++;
                        counter = 0;
                    }
                }
            }
        });
    }

    private Map<String, IncomingOutgoingPacket> calculateNumberOfPackets() {
        Map<String, IncomingOutgoingPacket> numberDict = new HashMap<>();

        // Defensive: if deviceSniffer or packets null, return empty map
        if (deviceSniffer == null || deviceSniffer.getDefaultPackets() == null) {
            return numberDict;
        }

        for (DefaultPacket defaultPacket : deviceSniffer.getDefaultPackets()) {
            for (String key : knownHosts.keySet()) {
                KnownHost knownHost = knownHosts.get(key);
                String src = defaultPacket.getSrcIp();
                String dst = defaultPacket.getDstIp();
                if (knownHost.containsIp(src) || knownHost.containsIp(dst)) {
                    IncomingOutgoingPacket numbers = numberDict.get(key);
                    if (numbers == null) {
                        numbers = new IncomingOutgoingPacket(0L, 0L, knownHost);
                    }
                    // increment incoming if src belongs to knownHost
                    if (knownHost.containsIp(src)) {
                        numbers.setIncoming(numbers.getIncoming() + 1);
                    }
                    // increment outgoing if dst belongs to knownHost
                    if (knownHost.containsIp(dst)) {
                        numbers.setOutgoing(numbers.getOutgoing() + 1);
                    }
                    numberDict.put(key, numbers);
                }
            }
        }
        return numberDict;
    }

    private void addToPane(IncomingOutgoingPacket incomingOutgoingPacket, int paneColumn) {
        VBox vBox;
        if (paneColumn == 0) {
            vBox = vboxPane;
        } else if (paneColumn == 1) {
            vBox = vboxPane1;
        } else {
            vBox = vboxPane2;
        }

        InputStream is = graphicalNetworkTracerFactory.getResource(incomingOutgoingPacket.getKnownHost().getIconPath());
        ImageView icon = new ImageView(new Image(is));
        icon.setFitHeight(92);
        icon.setFitWidth(93);
        icon.setPreserveRatio(true);

        int totalPackets = deviceSniffer.getDefaultPackets().size();
        double outgoingRatio = totalPackets > 0 ? (double) incomingOutgoingPacket.getOutgoing() / totalPackets : 0;
        double incomingRatio = totalPackets > 0 ? (double) incomingOutgoingPacket.getIncoming() / totalPackets : 0;

        Label incomingLabel = new Label("Incoming Packets");
        ProgressBar redProgress = new ProgressBar(incomingRatio);
        redProgress.setPrefWidth(300);
        redProgress.setStyle("-fx-accent: red;");

        Label outgoingLabel = new Label("Outgoing Packets");
        ProgressBar greenProgress = new ProgressBar(outgoingRatio);
        greenProgress.setPrefWidth(300);
        greenProgress.setStyle("-fx-accent: green;");

        VBox progressVBox = new VBox(10, incomingLabel, redProgress, outgoingLabel, greenProgress);
        HBox.setHgrow(progressVBox, Priority.ALWAYS);

        HBox rowBox = new HBox(10, icon, progressVBox);
        rowBox.setAlignment(Pos.CENTER_LEFT);
        rowBox.setPrefHeight(100);
        rowBox.setPrefWidth(400);
        rowBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 10;");

        vBox.getChildren().add(rowBox);
    }
}
