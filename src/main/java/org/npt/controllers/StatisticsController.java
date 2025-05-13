package org.npt.controllers;

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
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.npt.exception.ShutdownException;
import org.npt.models.DefaultPacket;
import org.npt.models.KnownHost;
import org.npt.models.Target;
import org.npt.networkservices.ArpSpoofStarter;
import org.npt.networkservices.PacketSniffer;
import org.npt.services.KnownHostService;
import org.npt.services.ResourceLoader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
class IncomingOutgoingPacket {

    private Long incoming;
    private Long outgoing;
}

public class StatisticsController {

    public VBox vboxPane2;
    public VBox vboxPane1;
    @FXML
    private VBox vboxPane;

    @Getter
    private Target target;

    private Timeline refreshTimeline;

    private PacketSniffer packetSniffer;

    @FXML
    public void initialize() {
        ArpSpoofStarter arpSpoofStarter = ArpSpoofStarter.getInstance();
        Optional<PacketSniffer> optionalPacketSniffer = arpSpoofStarter.getPacketSnifferByTarget(target);
        packetSniffer = optionalPacketSniffer.get();
        startRepeatingUpdates();

    }

    private void startRepeatingUpdates() {
        refreshTimeline = new Timeline(
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

        vboxPane.getChildren().clear(); // Optional: to avoid duplicate rows

        KnownHostService knownHostService = KnownHostService.getInstance();
        HashMap<String, KnownHost> knownHosts = knownHostService.getKnownHosts();
        ResourceLoader resourceLoader = ResourceLoader.getInstance();

        HashMap<String, IncomingOutgoingPacket> data = calculateNumberOfPackets();

        for (String key:knownHosts.keySet()){
            IncomingOutgoingPacket incomingOutgoingPacket = data.get(key);
            if(incomingOutgoingPacket != null){
                KnownHost knownHost = knownHosts.get(key);
                InputStream is = resourceLoader.getResource(knownHost.getIconPath());
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

                vboxPane.getChildren().add(rowBox);
            }
        }
    }

    public HashMap<String, IncomingOutgoingPacket> calculateNumberOfPackets() throws ShutdownException {
        KnownHostService knownHostService = KnownHostService.getInstance();
        HashMap<String, KnownHost> knownHosts = knownHostService.getKnownHosts();
        HashMap<String, IncomingOutgoingPacket> numberDict = new HashMap<>();

        for (DefaultPacket defaultPacket:packetSniffer.getDefaultPackets()){
            for (String key:knownHosts.keySet()){
                KnownHost knownHost = knownHosts.get(key);
                IncomingOutgoingPacket numbers = numberDict.getOrDefault(key,new IncomingOutgoingPacket(0L, 0L));
                String src = defaultPacket.getSrcIp();
                String dst = defaultPacket.getDstIp();
                if(knownHost.containsIp(src)){
                    numbers.setIncoming(numbers.getIncoming() + 1);
                }
                if(knownHost.containsIp(dst)){
                    numbers.setOutgoing(numbers.getOutgoing() + 1);
                }
            }
        }
        return numberDict;
    }

    public void setData(Target target) {
        this.target = target;
    }
}
