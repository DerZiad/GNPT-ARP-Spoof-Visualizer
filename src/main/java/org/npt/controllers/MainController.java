package org.npt.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.npt.beans.ResourceLoader;
import org.npt.beans.implementation.ResourceLoaderImpl;
import org.npt.configuration.Configuration;
import org.npt.exception.GatewayNotFoundException;
import org.npt.exception.ProcessFailureException;
import org.npt.models.Device;
import org.npt.models.Gateway;
import org.npt.models.SelfDevice;
import org.npt.models.Target;
import org.npt.services.impl.PicassoService;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.npt.configuration.Configuration.*;

public class MainController {

    public TextField ipAddress;
    public TextField deviceInterface;
    public Button addDevice;
    public TextField deviceName;
    private double animationProgress = 0.0;

    private Device draggedDevice = null;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean draggingRouter = false;
    private Font textSize = Font.font(14);
    private final PicassoService picassoService = new PicassoService();


    @FXML
    private Canvas canvas;

    @FXML
    private AnchorPane settingButton;

    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private MenuButton menuButton;

    private Image computerImage;
    private Image routerImage;
    private Image hackerComputerImage;

    @FXML
    public void initialize() {
        // Load images
        ResourceLoader resourceLoader = ResourceLoaderImpl.getInstance();
        computerImage = new Image(resourceLoader.getResource("computer.png"));
        routerImage = new Image(resourceLoader.getResource("router.png"));
        hackerComputerImage = new Image(resourceLoader.getResource("hacker.png"));

        // Bind Canvas size to AnchorPane size
        canvas.widthProperty().bind(rootAnchorPane.widthProperty());
        canvas.heightProperty().bind(rootAnchorPane.heightProperty());

        // Set up listeners for drawing
        //canvas.widthProperty().addListener((obs, oldVal, newVal) -> drawNetwork());
        //canvas.heightProperty().addListener((obs, oldVal, newVal) -> drawNetwork());

        settingButton.getStyleClass().add("anchor-pane-border");

        initializeCanvas();

        addDevice.setOnAction(_ -> {
            String ipAddress = this.ipAddress.getText();
            String deviceInterface = this.deviceInterface.getText();
            String deviceName = this.deviceInterface.getText();
            /*Device device = Device.builder()
                    .deviceName(deviceName)
                    .ipAddress(ipAddress)
                    .deviceInterface(deviceInterface)
                    .x(0)
                    .y(0)
                    .contextMenu(new ContextMenu())
                    .next(gateway)
                    .type(Type.TARGET)
                    .build();
            picassoService.initMenu(device.getContextMenu(),device);
            Configuration.devices.add(device);*/
            initializeCanvas();
        });
        initializeInterfaces();
    }

    private void initializeInterfaces() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                MenuItem menuItem = new MenuItem(networkInterface.getName());
                menuItem.setOnAction(event -> {
                    menuButton.setText(menuItem.getText());
                });
                menuButton.getItems().add(menuItem);
            }
            if (menuButton.getItems().isEmpty()) {
                menuButton.setText("No network");
            } else {
                String interfaceFound = menuButton.getItems().getFirst().getText();
                menuButton.setText(interfaceFound);
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeCanvas() {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        centerMyComputer();
        initializeGateways();
        drawConnectionsBetweenNetworkAndComputer(graphicsContext);
        for (Gateway gateway:gateways){
            drawRouter(graphicsContext,gateway);
        }
        //setupMouseEvents();
        //startConnectionAnimation();
        //drawNetwork();
    }

    private void drawConnectionsBetweenNetworkAndComputer(GraphicsContext gc){
        for (Gateway gateway:gateways){
            drawConnection(gc,gateway,selfDevice);
        }
    }

    private void centerMyComputer() {
        selfDevice.setX(canvas.getWidth() / 2);
        selfDevice.setY(canvas.getHeight() / 2);

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            selfDevice.setX(canvas.getWidth() / 2);
            //drawNetwork();
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            selfDevice.setY(newVal.doubleValue() / 2);
            //drawNetwork();
        });
    }

    private void initializeGateways() {
        int numberOfDevices = gateways.size();
        double radius = Math.min(canvas.getWidth(), canvas.getHeight()) / 3; // Distance from router
        double centerX = selfDevice.getX();
        double centerY = selfDevice.getY();

        for (int i = 0; i < numberOfDevices; i++) {
            double angle = 2 * Math.PI * i / numberOfDevices;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            Gateway gateway = gateways.get(i);
            gateway.setX(x);
            gateway.setY(y);
        }
    }



    private void drawConnection(GraphicsContext gc, Device startLine, Device endLine) {
        gc.strokeLine(startLine.getX(), startLine.getY(), endLine.getX(), endLine.getY());
    }

    private void drawRouter(GraphicsContext gc, Gateway gateway) {
        double routerSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        gc.drawImage(routerImage, gateway.getX() - routerSize / 2, gateway.getY() - routerSize / 2, routerSize, routerSize);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));
        gc.fillText(gateway.getDeviceName(), gateway.getX() - 25, gateway.getY() + routerSize / 2 + 20);
        gc.fillText(gateway.getIpAddresses().getFirst().getIp(), gateway.getX() - 30, gateway.getY() + routerSize / 2 + 35);
    }

}
