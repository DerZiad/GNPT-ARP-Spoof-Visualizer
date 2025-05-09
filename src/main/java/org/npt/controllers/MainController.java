package org.npt.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import org.npt.data.DataService;
import org.npt.data.GatewayService;
import org.npt.data.TargetService;
import org.npt.data.defaults.DefaultDataService;
import org.npt.data.defaults.DefaultGatewayService;
import org.npt.data.defaults.DefaultTargetService;
import org.npt.exception.InvalidInputException;
import org.npt.models.Device;
import org.npt.models.Gateway;
import org.npt.models.SelfDevice;
import org.npt.models.Target;
import org.npt.services.ResourceLoader;
import org.npt.services.impl.MainControllerServiceImpl;
import org.npt.services.impl.ResourceLoaderImpl;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@Slf4j
public class MainController {

    public TextField ipAddress;
    public Button addDevice;
    public TextField deviceName;
    private Device draggedDevice = null;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean draggingRouter = false;
    private final MainControllerServiceImpl mainControllerServiceImpl = new MainControllerServiceImpl();

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

    private final TargetService targetService = new DefaultTargetService();
    private final DataService dataService = DefaultDataService.getInstance();
    private final GatewayService gatewayService = new DefaultGatewayService();

    @FXML
    public void initialize() {
        // Devices
        dataService.getDevices().forEach(mainControllerServiceImpl::initMenu);

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
            try {
                Target target = targetService.create(deviceName, deviceInterface, new String[]{ipAddress});
                Optional<Gateway> gatewayOptional = gatewayService.find().stream().filter(gateway -> gateway.getNetworkInterface().equals(deviceInterface))
                        .findAny();
                gatewayOptional.ifPresent(associatedGateway -> associatedGateway.getDevices().add(target));
                mainControllerServiceImpl.initMenu(target);
                initializeCanvas();
            } catch (InvalidInputException e) {
                // TODO Handle exceptions in design
                throw new RuntimeException(e);
            }
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
        drawComputer(graphicsContext, dataService.getSelfDevice());
        calculateGatewaysPosition();
        drawRouters(graphicsContext);
        setupMouseEvents();
        double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        graphicsContext.setStroke(Color.GRAY);
        graphicsContext.setLineWidth(1.5);

        for (Target target : targetService.find()) {
            Image selectedComputerImage = computerImage;
            graphicsContext.drawImage(selectedComputerImage, target.getX() - imageSize / 2, target.getY() - imageSize / 2, imageSize, imageSize);
            graphicsContext.setFill(Color.BLACK);
            graphicsContext.setFont(Font.font(12));
            graphicsContext.fillText(target.getDeviceName(), target.getX() - 20, target.getY() + imageSize / 2 + 15);
            graphicsContext.fillText(target.getIpAddresses().getFirst(), target.getX() - 20, target.getY() + imageSize / 2 + 30);
        }

        for (Gateway gateway : gatewayService.find()) {
            drawConnection(graphicsContext, dataService.getSelfDevice(), gateway);
            for (Target target : gateway.getDevices()) {
                drawConnection(graphicsContext, gateway, target);
            }
        }

    }

    public void calculateGatewaysPosition() {
        List<Gateway> gateways = gatewayService.find().stream().toList();
        int gatewaysSize = gateways.size();
        if (gatewaysSize == 0) return;  // Prevent division by zero

        double xCenter = dataService.getSelfDevice().getX();
        double yCenter = dataService.getSelfDevice().getY();
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
        for (Gateway gateway : gatewayService.find()) {
            gc.drawImage(routerImage, gateway.getX() - routerSize / 2, gateway.getY() - routerSize / 2, routerSize, routerSize);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(14));
            gc.fillText(gateway.getDeviceName(), gateway.getX() - 25, gateway.getY() + routerSize / 2 + 20);
            gc.fillText(gateway.getNetworkInterface(), gateway.getX() - 30, gateway.getY() + routerSize / 2 + 35);
        }
    }

    private void centerSelfDevice() {
        SelfDevice selfDevice = dataService.getSelfDevice();
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

            for (Device device : dataService.getDevices()) {
                if (verifyClickInsideImage.test(device, event)) {
                    device.getContextMenu().show(canvas, event.getScreenX(), event.getScreenY());
                    return;
                }
            }
        } else {
            SelfDevice selfDevice = dataService.getSelfDevice();
            if (event.getX() >= selfDevice.getX() - imageSize / 2 && event.getX() <= selfDevice.getX() + imageSize / 2 &&
                    event.getY() >= selfDevice.getY() - imageSize / 2 && event.getY() <= selfDevice.getY() + imageSize / 2) {
                draggingRouter = true;
                dragOffsetX = event.getX() - selfDevice.getX();
                dragOffsetY = event.getY() - selfDevice.getY();
                return;
            }

            for (Device device : dataService.getDevices()) {
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
        SelfDevice selfDevice = dataService.getSelfDevice();
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
