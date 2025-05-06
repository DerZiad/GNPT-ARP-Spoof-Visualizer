package org.npt.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import org.npt.beans.ResourceLoader;
import org.npt.beans.implementation.ResourceLoaderImpl;
import org.npt.models.*;
import org.npt.services.impl.PicassoService;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.function.BiPredicate;

import static org.npt.configuration.Configuration.*;

@Slf4j
public class MainController {

    private final List<Device> devices = new ArrayList<>();
    public TextField ipAddress;
    public Button addDevice;
    public TextField deviceName;
    private Device draggedDevice = null;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean draggingRouter = false;
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
        // Devices
        devices.add(selfDevice);
        devices.addAll(targets);
        devices.addAll(gateways);
        devices.forEach(picassoService::initMenu);

        // Load images
        ResourceLoader resourceLoader = ResourceLoaderImpl.getInstance();
        computerImage = new Image(resourceLoader.getResource("computer.png"));
        routerImage = new Image(resourceLoader.getResource("router.png"));
        hackerComputerImage = new Image(resourceLoader.getResource("hacker.png"));

        // Bind Canvas size to AnchorPane size
        canvas.widthProperty().bind(rootAnchorPane.widthProperty());
        canvas.heightProperty().bind(rootAnchorPane.heightProperty());

        // Set up listeners for drawing
        canvas.widthProperty().addListener((_, _, _) -> initializeCanvas());
        canvas.heightProperty().addListener((_, _, _) -> initializeCanvas());

        settingButton.getStyleClass().add("anchor-pane-border");
        addDevice.setOnAction(_ -> {
            String ipAddress = this.ipAddress.getText();
            String deviceInterface = this.menuButton.getText();
            String deviceName = this.deviceName.getText();
            Target target = new Target(deviceName, deviceInterface, List.of(ipAddress), 0, 0, new ContextMenu());
            devices.add(target);
            targets.add(target);
            Optional<Gateway> gatewayOptional = gateways.stream().filter(gateway -> gateway.getNetworkInterface().equals(deviceInterface))
                    .findAny();
            gatewayOptional.ifPresent(associatedGateway -> associatedGateway.getDevices().add(target));
            picassoService.initMenu(target);
            initializeCanvas();
        });
        initializeInterfaces();
        centerSelfDevice();
        initializeCanvas();
    }

    private void initializeInterfaces() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                MenuItem menuItem = new MenuItem(networkInterface.getName());
                menuItem.setOnAction(_ -> {
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
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawComputer(graphicsContext, selfDevice);
        calculateGatewaysPosition();
        drawRouters(graphicsContext);
        setupMouseEvents();
        double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        graphicsContext.setStroke(Color.GRAY);
        graphicsContext.setLineWidth(1.5);

        for (Target target : targets) {
            Image selectedComputerImage = computerImage;
            graphicsContext.drawImage(selectedComputerImage, target.getX() - imageSize / 2, target.getY() - imageSize / 2, imageSize, imageSize);
            graphicsContext.setFill(Color.BLACK);
            graphicsContext.setFont(Font.font(12));
            graphicsContext.fillText(target.getDeviceName(), target.getX() - 20, target.getY() + imageSize / 2 + 15);
            graphicsContext.fillText(target.getIpAddresses().getFirst(), target.getX() - 20, target.getY() + imageSize / 2 + 30);
        }

        for (Gateway gateway : gateways) {
            drawConnection(graphicsContext, selfDevice, gateway);
            for (Target target : gateway.getDevices()) {
                drawConnection(graphicsContext, gateway, target);
            }
        }

    }

    public void calculateGatewaysPosition() {
        int gatewaysSize = gateways.size();
        if (gatewaysSize == 0) return;  // Prevent division by zero

        double xCenter = selfDevice.getX();
        double yCenter = selfDevice.getY();
        double R = Math.min(canvas.getWidth(), canvas.getHeight()) / 3;
        ;

        double step = 2 * Math.PI / gatewaysSize;

        for (int i = 0; i < gatewaysSize; i++) {
            double angle = step * i;
            double x = xCenter + R * Math.cos(angle);
            double y = yCenter + R * Math.sin(angle);

            Gateway gateway = gateways.get(i);
            gateway.setX(x);
            gateway.setY(y);
        }
    }

    private void drawRouters(GraphicsContext gc) {
        double routerSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        for (Gateway gateway : gateways) {
            gc.drawImage(routerImage, gateway.getX() - routerSize / 2, gateway.getY() - routerSize / 2, routerSize, routerSize);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(14));
            gc.fillText(gateway.getDeviceName(), gateway.getX() - 25, gateway.getY() + routerSize / 2 + 20);
            gc.fillText(gateway.getNetworkInterface(), gateway.getX() - 30, gateway.getY() + routerSize / 2 + 35);
        }
    }

    private void centerSelfDevice() {
        selfDevice.setX(canvas.getWidth() / 2);
        selfDevice.setY(canvas.getHeight() / 2);

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            selfDevice.setX(canvas.getWidth() / 2);
            initializeCanvas();
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            selfDevice.setY(newVal.doubleValue() / 2);
            initializeCanvas();
        });
    }

    private void setupMouseEvents() {
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(event -> onMouseReleased());
    }

    private void onMousePressed(MouseEvent event) {
        double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        if (event.getButton() == MouseButton.SECONDARY) {
            BiPredicate<Device, MouseEvent> verifyClickInsideImage = new BiPredicate<>() {
                @Override
                public boolean test(Device device, MouseEvent event) {
                    double xLeftBorder = device.getX() - imageSize / 2;
                    double xRightBorder = device.getX() + imageSize / 2;
                    double yTopBorder = device.getY() - imageSize / 2;
                    double yBottomBorder = device.getY() + imageSize / 2;
                    double x = event.getX();
                    double y = event.getY();
                    return x <= xRightBorder && x >= xLeftBorder && y <= yBottomBorder && y >= yTopBorder;
                }
            };

            for (Device device : devices) {
                if (verifyClickInsideImage.test(device, event)) {
                    device.getContextMenu().show(canvas, event.getScreenX(), event.getScreenY());
                    return;
                }
            }
        } else {
            if (event.getX() >= selfDevice.getX() - imageSize / 2 && event.getX() <= selfDevice.getX() + imageSize / 2 &&
                    event.getY() >= selfDevice.getY() - imageSize / 2 && event.getY() <= selfDevice.getY() + imageSize / 2) {
                draggingRouter = true;
                dragOffsetX = event.getX() - selfDevice.getX();
                dragOffsetY = event.getY() - selfDevice.getY();
                return;
            }

            for (Device device : devices) {
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
            selfDevice.setX(event.getX() - dragOffsetX);
            selfDevice.setY(event.getY() - dragOffsetY);
            initializeCanvas();
        } else if (draggedDevice != null) {
            draggedDevice.setX(event.getX() - dragOffsetX);
            draggedDevice.setY(event.getY() - dragOffsetY);
            initializeCanvas();
        }
    }

    private void onMouseReleased() {
        draggedDevice = null;
        draggingRouter = false;
    }

    private void drawConnection(GraphicsContext gc, Device startLine, Device endLine) {
        gc.strokeLine(startLine.getX(), startLine.getY(), endLine.getX(), endLine.getY());
    }

    private void drawComputer(GraphicsContext gc, SelfDevice selfDevice) {
        double routerSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        gc.drawImage(hackerComputerImage, selfDevice.getX() - routerSize / 2, selfDevice.getY() - routerSize / 2, routerSize, routerSize);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));
        gc.fillText(selfDevice.getDeviceName(), selfDevice.getX() - 25, selfDevice.getY() + routerSize / 2 + 20);
        //gc.fillText(selfDevice.getIpAddresses().getFirst().getIp(), selfDevice.getX() - 30, selfDevice.getY() + routerSize / 2 + 35);
    }

}
