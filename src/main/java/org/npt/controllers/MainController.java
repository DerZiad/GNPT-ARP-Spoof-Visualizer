package org.npt.controllers;

import javafx.animation.PauseTransition;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.npt.data.DataService;
import org.npt.data.GatewayService;
import org.npt.data.TargetService;
import org.npt.data.defaults.DefaultDataService;
import org.npt.data.defaults.DefaultGatewayService;
import org.npt.data.defaults.DefaultTargetService;
import org.npt.exception.InvalidInputException;
import org.npt.models.*;
import org.npt.services.ResourceLoader;
import org.npt.services.impl.MainControllerServiceImpl;
import org.npt.services.impl.ResourceLoaderImpl;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

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
    public VBox vboxPane;

    @FXML
    private Canvas canvas;

    @FXML
    private MenuButton menuButton;

    @FXML
    private BorderPane borderPane;

    private final HashMap<Class, Image> images = new HashMap<>();
    private final TargetService targetService = new DefaultTargetService();
    private final DataService dataService = DefaultDataService.getInstance();
    private final GatewayService gatewayService = new DefaultGatewayService();

    @FXML
    public void initialize() {
        // Devices
        dataService.getDevices().forEach(device -> mainControllerServiceImpl.initMenu(device, () -> initializeCanvas(canvas)));

        // Load images
        ResourceLoader resourceLoader = ResourceLoaderImpl.getInstance();
        images.put(Target.class, new Image(resourceLoader.getResource("computer.png")));
        images.put(Gateway.class, new Image(resourceLoader.getResource("router.png")));
        images.put(SelfDevice.class, new Image(resourceLoader.getResource("hacker.png")));

        canvas.widthProperty().bind(borderPane.widthProperty());
        canvas.heightProperty().bind(borderPane.heightProperty());
        borderPane.setMinSize(0, 0);

        // Set up listeners for drawing
        canvas.widthProperty().addListener((_, _, _) -> {
            initializeCanvas(canvas);
            centerSelfDevice();
            calculateGatewaysPosition();
        });
        canvas.heightProperty().addListener((_, _, _) -> {
            initializeCanvas(canvas);
            centerSelfDevice();
            calculateGatewaysPosition();
        });
        mainControllerServiceImpl.initMenu(dataService.getSelfDevice(), () -> initializeCanvas(canvas));

        addDevice.setOnAction(_ -> {
            String ipAddress = this.ipAddress.getText();
            String deviceInterface = this.menuButton.getText();
            String deviceName = this.deviceName.getText();
            try {
                Target target = targetService.create(deviceName, deviceInterface, new String[]{ipAddress});
                Optional<Gateway> gatewayOptional = gatewayService.find().stream().filter(gateway -> gateway.getNetworkInterface().equals(deviceInterface))
                        .findAny();
                gatewayOptional.ifPresent(associatedGateway -> associatedGateway.getDevices().add(target));
                mainControllerServiceImpl.initMenu(target, () -> initializeCanvas(canvas));
                initializeCanvas(canvas);
            } catch (InvalidInputException e) {
                // TODO Handle exceptions in design
                throw new RuntimeException(e);
            }
        });
        initializeInterfaces();
        centerSelfDevice();
        calculateGatewaysPosition();
        setupMouseEvents();
        initializeCanvas(canvas);
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

    public void initializeCanvas(Canvas canvas) {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(3);
        double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        SelfDevice selfDevice = dataService.getSelfDevice();
        draw(graphicsContext, selfDevice, imageSize, SelfDevice.class);
        Collection<Gateway> gateways = gatewayService.find();

        for (Gateway gateway : gateways) {
            draw(graphicsContext, gateway, imageSize, Gateway.class);
            drawConnection(graphicsContext, gateway, selfDevice);
            for (Target target : gateway.getDevices()) {
                draw(graphicsContext, target, imageSize, Target.class);
                drawConnection(graphicsContext, gateway, target);
            }
        }
    }

    public void calculateGatewaysPosition() {
        Collection<Gateway> gateways = gatewayService.find();
        int gatewaysSize = gateways.size();
        if (gatewaysSize == 0) return;
        double xCenter = dataService.getSelfDevice().getX();
        double yCenter = dataService.getSelfDevice().getY();
        double R = Math.min(canvas.getWidth(), canvas.getHeight()) / 3;
        double step = 2 * Math.PI / gatewaysSize;

        Iterator<Gateway> iterator = gateways.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            double angle = step * i;
            double x = xCenter + R * Math.cos(angle);
            double y = yCenter + R * Math.sin(angle);
            Gateway gateway = iterator.next();
            gateway.setX(x);
            gateway.setY(y);
            i++;
        }
    }

    private void centerSelfDevice() {
        SelfDevice selfDevice = dataService.getSelfDevice();
        selfDevice.setX(canvas.getWidth() / 2);
        selfDevice.setY(canvas.getHeight() / 2);

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            selfDevice.setX(canvas.getWidth() / 2);
            initializeCanvas(canvas);
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            selfDevice.setY(newVal.doubleValue() / 2);
            initializeCanvas(canvas);
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
            BiPredicate<Device, MouseEvent> verifyClickInsideImage = (device, event1) -> {
                double xLeftBorder = device.getX() - imageSize / 2;
                double xRightBorder = device.getX() + imageSize / 2;
                double yTopBorder = device.getY() - imageSize / 2;
                double yBottomBorder = device.getY() + imageSize / 2;
                double x = event1.getX();
                double y = event1.getY();
                return x <= xRightBorder && x >= xLeftBorder && y <= yBottomBorder && y >= yTopBorder;
            };

            SelfDevice selfDevice = dataService.getSelfDevice();
            if (verifyClickInsideImage.test(selfDevice, event)) {
                selfDevice.getContextMenu().show(canvas, event.getScreenX(), event.getScreenY());
                return;
            }

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
            double dx = event.getX() - dragOffsetX;
            double dy = event.getY() - dragOffsetY;
            if (dx < 0 || dx > canvas.getWidth() || dy > canvas.getHeight() || dy < 0)
                return;
            selfDevice.setX(dx);
            selfDevice.setY(dy);
            PauseTransition pause = new PauseTransition(Duration.millis(10));
            pause.setOnFinished(e -> {
                initializeCanvas(canvas);
            });
            pause.play();
        } else if (draggedDevice != null) {
            double dx = event.getX() - dragOffsetX;
            double dy = event.getY() - dragOffsetY;
            if (dx < 0 || dx > canvas.getWidth() || dy > canvas.getHeight() || dy < 0)
                return;
            draggedDevice.setX(dx);
            draggedDevice.setY(dy);
            PauseTransition pause = new PauseTransition(Duration.millis(10));
            pause.setOnFinished(e -> {
                initializeCanvas(canvas);
            });
            pause.play();
        }
    }

    private void onMouseReleased() {
        draggedDevice = null;
        draggingRouter = false;
    }

    private void drawConnection(GraphicsContext gc, Device startLine, Device endLine) {
        double r = 35;
        double x1 = startLine.getX();
        double y1 = startLine.getY();
        double x2 = endLine.getX();
        double y2 = endLine.getY();

        if (x1 == x2) {
            double dy = (y2 > y1 ? 1 : -1) * r;
            gc.strokeLine(x1, y1 + dy, x2, y2);
            return;
        }

        double a = (y2 - y1) / (x2 - x1);
        double b = y2 - a * x2;

        Function<Double, Double> lineEquation = x -> a * x + b;

        BiFunction<Double[], Boolean, Double[]> calculateSolution = (center, inverse) -> {
            double xc = center[0];
            double yc = center[1];
            double a1 = 1 + a * a;
            double b1 = -2 * xc + 2 * a * (b - yc);
            double c = xc * xc + Math.pow(b - yc, 2) - r * r;

            double delta = b1 * b1 - 4 * a1 * c;
            if (delta < 0) {
                return new Double[]{xc, yc};
            }

            final double sqrtDelta = Math.sqrt(delta);
            double solX;
            if (!inverse) {
                solX = x1 < x2 ? (-b1 + sqrtDelta) / (2 * a1) : (-b1 - sqrtDelta) / (2 * a1);
            } else {
                solX = x1 < x2 ? (-b1 - sqrtDelta) / (2 * a1) : (-b1 + sqrtDelta) / (2 * a1);
            }
            double solY = lineEquation.apply(solX);
            return new Double[]{solX, solY};
        };

        Double[] p1 = calculateSolution.apply(new Double[]{x1, y1}, false);
        Double[] p2 = calculateSolution.apply(new Double[]{x2, y2}, true);
        if (mainControllerServiceImpl.getDevices().contains(startLine) || mainControllerServiceImpl.getDevices().contains(endLine))
            gc.setStroke(Color.RED);
        else
            gc.setStroke(Color.BLACK);
        gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
    }

    private void draw(GraphicsContext gc, Device device, double imageSize, Class deviceClass) {
        gc.drawImage(images.get(deviceClass), device.getX() - imageSize / 2, device.getY() - imageSize / 2, imageSize, imageSize);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));
        gc.fillText(device.getDeviceName(), device.getX() - 25, device.getY() + imageSize / 2 + 20);
        if (deviceClass.equals(SelfDevice.class)) {
            Optional<IpAddress> ipAddress = ((SelfDevice) device).findFirstIPv4();
            ipAddress.ifPresent(ipString -> {
                gc.fillText(ipString.getNetworkInterface(), device.getX() - 25, device.getY() + imageSize / 2 + 40);
                gc.fillText(ipString.getIp(), device.getX() - 25, device.getY() + imageSize / 2 + 60);
            });
        } else if (deviceClass.equals(Target.class)) {
            Target target = (Target) device;
            gc.fillText(target.getNetworkInterface(), target.getX() - 25, target.getY() + imageSize / 2 + 40);
            target.findFirstIPv4().ifPresent(ipString -> gc.fillText(ipString, target.getX() - 25, target.getY() + imageSize / 2 + 60));
        } else {
            Gateway gateway = (Gateway) device;
            gc.fillText(gateway.getNetworkInterface(), gateway.getX() - 25, gateway.getY() + imageSize / 2 + 40);
            gateway.findFirstIPv4().ifPresent(ipString -> gc.fillText(ipString, gateway.getX() - 25, gateway.getY() + imageSize / 2 + 60));
        }
    }

}
