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
import org.npt.beans.implementation.GatewayFinder;
import org.npt.beans.implementation.ResourceLoaderImpl;
import org.npt.configuration.Configuration;
import org.npt.exception.GatewayNotFoundException;
import org.npt.exception.ProcessFailureException;
import org.npt.models.Connection;
import org.npt.models.Device;
import org.npt.models.Type;

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
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> drawNetwork());
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> drawNetwork());

        settingButton.getStyleClass().add("anchor-pane-border");

        Device myDevice = new Device("Device3", "192.168.178.33", 0, 0, Type.SELF, new ContextMenu());
        Configuration.devices.add(myDevice);

        initializeCanvas();

        addDevice.setOnAction(_ -> {
            String ipAddress = this.ipAddress.getText();
            String deviceInterface = this.deviceInterface.getText();
            String deviceName = this.deviceInterface.getText();
            Device device = Device.builder()
                    .deviceName(deviceName)
                    .ipAddress(ipAddress)
                    .x(0)
                    .y(0)
                    .contextMenu(new ContextMenu())
                    .type(Type.TARGET)
                    .build();
            Configuration.devices.add(device);
            initializeCanvas();
        });
        initializeInterfaces();
    }

    public void initializeCanvas() {
        // Initialize the network configuration
        scan();

        // Center router and initialize device layout
        centerRouter();
        initializeDevices();
        setupMouseEvents();
        startConnectionAnimation();
        drawNetwork();
    }

    private void initializeInterfaces() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                MenuItem menuItem = new MenuItem(networkInterface.getName());
                menuItem.setOnAction(event -> {
                    scanInterface = menuItem.getText();
                    menuButton.setText(menuItem.getText());
                });
                menuButton.getItems().add(menuItem);
            }
            if (menuButton.getItems().isEmpty()) {
                menuButton.setText("No network");
            } else {
                String interfaceFound = menuButton.getItems().getFirst().getText();
                menuButton.setText(interfaceFound);
                scanInterface = interfaceFound;
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private void centerRouter() {
        gateway.setX(canvas.getWidth() / 2);
        gateway.setY(canvas.getHeight() / 2);

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            gateway.setX(canvas.getWidth() / 2);
            drawNetwork();
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            gateway.setY(newVal.doubleValue() / 2);
            drawNetwork();
        });
    }

    private void initializeDevices() {
        int numberOfDevices = devices.size();
        double radius = Math.min(canvas.getWidth(), canvas.getHeight()) / 3; // Distance from router
        double centerX = gateway.getX();
        double centerY = gateway.getY();

        for (int i = 0; i < numberOfDevices; i++) {
            double angle = 2 * Math.PI * i / numberOfDevices;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            devices.get(i).setX(x);
            devices.get(i).setY(y);
        }
    }

    private void setupMouseEvents() {
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(event -> onMouseReleased());
    }

    private void onMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            for (Device device : devices) {
                double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
                if (event.getX() >= device.getX() - imageSize / 2 && event.getX() <= device.getX() + imageSize / 2 &&
                        event.getY() >= device.getY() - imageSize / 2 && event.getY() <= device.getY() + imageSize / 2) {
                    device.getContextMenu().show(canvas, event.getScreenX(), event.getScreenY());
                    return;
                }
            }
        } else {
            double routerSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
            if (event.getX() >= gateway.getX() - routerSize / 2 && event.getX() <= gateway.getX() + routerSize / 2 &&
                    event.getY() >= gateway.getY() - routerSize / 2 && event.getY() <= gateway.getY() + routerSize / 2) {
                draggingRouter = true;
                dragOffsetX = event.getX() - gateway.getX();
                dragOffsetY = event.getY() - gateway.getY();
                return;
            }

            for (Device device : devices) {
                double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
                if (event.getX() >= device.getX() - imageSize / 2 && event.getX() <= device.getX() + imageSize / 2 &&
                        event.getY() >= device.getY() - imageSize / 2 && event.getY() <= device.getY() + imageSize / 2) {
                    draggedDevice = device;
                    dragOffsetX = event.getX() - device.getX();
                    dragOffsetY = event.getY() - device.getY();
                    break;
                }
            }
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (draggingRouter) {
            gateway.setX(event.getX() - dragOffsetX);
            gateway.setY(event.getY() - dragOffsetY);
            drawNetwork();
        } else if (draggedDevice != null) {
            draggedDevice.setX(event.getX() - dragOffsetX);
            draggedDevice.setY(event.getY() - dragOffsetY);
            drawNetwork();
        }
    }

    private void onMouseReleased() {
        draggedDevice = null;
        draggingRouter = false;
    }

    private void drawNetwork() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawRouter(gc);

        double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1.5);

        for (Device device : devices) {
            Image selectedComputerImage = computerImage;
            if (device.getType().equals(Type.SELF)) {
                selectedComputerImage = hackerComputerImage;
            }
            gc.drawImage(selectedComputerImage, device.getX() - imageSize / 2, device.getY() - imageSize / 2, imageSize, imageSize);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(12));
            gc.fillText(device.getDeviceName(), device.getX() - 20, device.getY() + imageSize / 2 + 15);
            gc.fillText(device.getIpAddress(), device.getX() - 20, device.getY() + imageSize / 2 + 30);
        }

        for (Connection connection : connections) {
            Device first = connection.getFirstDevice();
            Device second = connection.getSecondDevice();
            drawMovingDot(gc, first.getX(), first.getY(), second.getX(), second.getY());
        }
        drawConnections(gc);
    }

    private void drawRouter(GraphicsContext gc) {
        double routerSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        gc.drawImage(routerImage, gateway.getX() - routerSize / 2, gateway.getY() - routerSize / 2, routerSize, routerSize);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));
        gc.fillText(gateway.getDeviceName(), gateway.getX() - 25, gateway.getY() + routerSize / 2 + 20);
        gc.fillText(gateway.getIpAddress(), gateway.getX() - 30, gateway.getY() + routerSize / 2 + 35);
    }

    private void startConnectionAnimation() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            animationProgress += 0.05;
            if (animationProgress > 1.0) {
                animationProgress = 0.0;
            }
            drawNetwork();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void drawMovingDot(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        double dotX = startX + (endX - startX) * animationProgress;
        double dotY = startY + (endY - startY) * animationProgress;
        gc.setFill(Color.CYAN);
        gc.fillOval(dotX - 3, dotY - 3, 6, 6); // Dot size of 6x6 pixels
    }

    private void drawConnections(GraphicsContext gc) {
        connections.forEach((Connection connection) -> {
            gc.strokeLine(connection.getFirstDevice().getX(), connection.getFirstDevice().getY(), connection.getSecondDevice().getX(), connection.getSecondDevice().getY());
        });
    }

    public void scan() {
        try {
            GatewayFinder gatewayFinder = GatewayFinder.getInstance();
            Configuration.gateway = gatewayFinder.getGateway();
            devices.forEach(device -> connections.add(new Connection(gateway, device)));
        } catch (ProcessFailureException | GatewayNotFoundException e) {
            e.printStackTrace();
        }
    }
}
