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
import org.npt.models.KnownHost;
import org.npt.models.Target;
import org.npt.networkservices.defaults.ArpSpoofStarterImpl;
import org.npt.networkservices.defaults.DefaultPacketSniffer;
import org.npt.services.KnownHostService;
import org.npt.services.ResourceLoader;
import org.npt.services.impl.DefaultKnownHostService;
import org.npt.services.impl.ResourceLoaderImpl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class StatisticsController {

    @FXML
    private VBox vboxPane;

    @Getter
    private Target target;

    private Timeline refreshTimeline;

    @FXML
    public void initialize() {
        startRepeatingUpdates();
    }

    private void startRepeatingUpdates() {
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> updateStatistics()),
                new KeyFrame(Duration.seconds(2))
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void updateStatistics() {
        if (target == null || target.findFirstIPv4().isEmpty()) return;

        vboxPane.getChildren().clear(); // Optional: to avoid duplicate rows

        KnownHostService knownHostService = DefaultKnownHostService.getInstance();
        HashMap<String, KnownHost> knownHosts = knownHostService.getKnownHosts();
        ResourceLoader resourceLoader = ResourceLoaderImpl.getInstance();

        KnownHost knownHost = knownHosts.get("facebook");

        InputStream is = resourceLoader.getResource(knownHost.getIconPath());
        ImageView icon = new ImageView(new Image(is));
        icon.setFitHeight(92);
        icon.setFitWidth(93);
        icon.setPreserveRatio(true);

        ArpSpoofStarterImpl arpSpoofStarter = ArpSpoofStarterImpl.getInstance();
        DefaultPacketSniffer defaultPacketSniffer = arpSpoofStarter.getPacketSniffers().stream()
                .filter(d1 -> d1.getTargetIp().equals(target.findFirstIPv4().get()))
                .collect(Collectors.toCollection(ArrayList::new))
                .getFirst();
        long numberOfPackets = knownHost.containsIp("157.240.27.35") ? 1:0;

        numberOfPackets += defaultPacketSniffer.getPackets().stream()
                .filter(packet -> knownHost.containsIp(packet.getSrcIp()) || knownHost.containsIp(packet.getDstIp()))
                .count();



        double totalPackets = defaultPacketSniffer.getPackets().size();
        double ratio = totalPackets == 0 ? 0 : (double) numberOfPackets / totalPackets;

        Label incomingLabel = new Label("Incoming Packets");
        ProgressBar redProgress = new ProgressBar(ratio);
        redProgress.setPrefWidth(300);
        redProgress.setStyle("-fx-accent: red;");

        Label outgoingLabel = new Label("Outgoing Packets");
        ProgressBar greenProgress = new ProgressBar(ratio);
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

    public void setData(Target target) {
        this.target = target;
    }
}
