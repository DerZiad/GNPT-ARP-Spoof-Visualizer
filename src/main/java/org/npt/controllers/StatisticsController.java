//package org.npt.controllers;

/*
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import lombok.*;
import org.npt.exception.ShutdownException;
import org.npt.models.DefaultPacket;
import org.npt.models.KnownHost;
import org.npt.models.Target;
import org.npt.services.ArpSpoofService;
import org.npt.services.KnownHostService;
import org.npt.services.ResourceLoader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class IncomingOutgoingPacket {

    private Long incoming;
    private Long outgoing;
    private KnownHost knownHost;
}

public class StatisticsController {

    private static final Integer MAX_ELEMENT_TO_DISPLAY_PER_PANE = 10;

    @FXML
    public VBox vboxPane2;

    @FXML
    public VBox vboxPane1;

    @FXML
    private VBox vboxPane;

    @Getter
    private Target target;

    private PacketSniffer packetSniffer;

    private static HashMap<String, KnownHost> knownHosts;

    private static ResourceLoader resourceLoader;

    public void initialize() throws ShutdownException {
        ArpSpoofService arpSpoofStarter = ArpSpoofService.getInstance();
        Optional<PacketSniffer> optionalPacketSniffer = arpSpoofStarter.getPacketSnifferByTarget(target);
        optionalPacketSniffer.ifPresentOrElse(packetSnifferNotNull -> packetSniffer = packetSnifferNotNull, () -> {
        });
        startRepeatingUpdates();
        if (knownHosts == null) {
            KnownHostService knownHostService = KnownHostService.getInstance();
            knownHosts = knownHostService.getKnownHosts();
        }

        if (resourceLoader == null)
            resourceLoader = ResourceLoader.getInstance();

    }

    private void startRepeatingUpdates() {
        Timeline refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> {
                    try {
                        updateStatistics();
                    } catch (ShutdownException ex) {
                        throw new RuntimeException(ex);
                    }
                }),
                new KeyFrame(Duration.seconds(2))
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }


    private void updateStatistics() throws ShutdownException {
        vboxPane.getChildren().clear();
        Map<String, IncomingOutgoingPacket> data = calculateNumberOfPackets();
        int counter = 0;
        int column = 0;
        for (String key : knownHosts.keySet()) {
            IncomingOutgoingPacket incomingOutgoingPacket = data.get(key);
            if (incomingOutgoingPacket != null) {
                addToPane(incomingOutgoingPacket, column);
                if (counter == 9) {
                    column++;
                    counter = 0;
                }
                counter++;
            }
        }
    }

    private Map<String, IncomingOutgoingPacket> calculateNumberOfPackets() throws ShutdownException {
        KnownHostService knownHostService = KnownHostService.getInstance();
        Map<String, KnownHost> knownHosts = knownHostService.getKnownHosts();
        Map<String, IncomingOutgoingPacket> numberDict = new HashMap<>();

        for (DefaultPacket defaultPacket : packetSniffer.getDefaultPackets()) {
            for (String key : knownHosts.keySet()) {
                KnownHost knownHost = knownHosts.get(key);
                String src = defaultPacket.getSrcIp();
                String dst = defaultPacket.getDstIp();
                if (knownHost.containsIp(src) || knownHost.containsIp(dst)) {
                    IncomingOutgoingPacket numbers = numberDict.get(key);
                    if (numbers == null) {
                        numbers = new IncomingOutgoingPacket(0L, 0L, knownHost);
                    }
                    numbers.setIncoming(numbers.getIncoming() + (knownHost.containsIp(src) ? 1 : 0));
                    numbers.setOutgoing(numbers.getOutgoing() + (knownHost.containsIp(dst) ? 1 : 0));
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
        InputStream is = resourceLoader.getResource(incomingOutgoingPacket.getKnownHost().getIconPath());
        ImageView icon = new ImageView(new Image(is));
        icon.setFitHeight(92);
        icon.setFitWidth(93);
        icon.setPreserveRatio(true);
        double outgoingRatio = (double) incomingOutgoingPacket.getOutgoing() / packetSniffer.getDefaultPackets().size();
        double incomingRatio = (double) incomingOutgoingPacket.getIncoming() / packetSniffer.getDefaultPackets().size();
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

    public void setData(Target target) {
        this.target = target;
    }
}
*/