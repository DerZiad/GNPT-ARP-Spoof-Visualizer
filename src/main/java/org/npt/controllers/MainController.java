package org.npt.controllers;

import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
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
import org.npt.models.*;
import org.npt.models.ui.DeviceUI;
import org.npt.uiservices.DeviceUiMapperService;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

@Slf4j
public class MainController extends DataInjector {

    private static DeviceUiMapperService deviceUiMapperService;
    private static final HashMap<Class<? extends Device>, Image> images = new HashMap<>();

    private DeviceUI draggedDevice = null;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean draggingRouter = false;

    @FXML
    public TextField ipAddress;

    @FXML
    public Button addDevice;

    @FXML
    public TextField deviceName;

    @FXML
    public VBox vboxPane;

    @FXML
    private Canvas canvas;

    @FXML
    private MenuButton menuButton;

    @FXML
    private BorderPane borderPane;

    @FXML
    public MenuItem newMenu;

    @FXML
    public MenuItem aboutGnptMenu;

    @FXML
    public void initialize() {
        deviceUiMapperService = new DeviceUiMapperService(() -> initCanvas(canvas));

        images.put(Target.class, new Image(graphicalNetworkTracerFactory.getResource("images/computer.png")));
        images.put(Gateway.class, new Image(graphicalNetworkTracerFactory.getResource("images/router.png")));
        images.put(SelfDevice.class, new Image(graphicalNetworkTracerFactory.getResource("images/hacker.png")));

        canvas.widthProperty().bind(borderPane.widthProperty());
        canvas.heightProperty().bind(borderPane.heightProperty());
        borderPane.setMinSize(0, 0);

        canvas.widthProperty().addListener((ignored1, ignored2, ignored3) -> {
            initCanvas(canvas);
            centerSelfDevice();
            calculateGatewaysPosition();
        });
        canvas.heightProperty().addListener((ignored1, ignored2, ignored3) -> {
            initCanvas(canvas);
            centerSelfDevice();
            calculateGatewaysPosition();
        });

        addDevice.setOnAction(ignored -> {
            final String ipAddress = this.ipAddress.getText();
            final String deviceInterface = this.menuButton.getText();
            final String deviceName = this.deviceName.getText();
            deviceUiMapperService.addTarget(ipAddress, deviceInterface, deviceName);
            this.ipAddress.setText("");
            this.deviceName.setText("");
        });

        newMenu.setOnAction(ignored -> {
            deviceUiMapperService.clear();
        });

        scanCurrentDeviceNetworkInterfaces();
        centerSelfDevice();
        calculateGatewaysPosition();
        setupMouseEvents();
        initCanvas(canvas);
    }

    private void scanCurrentDeviceNetworkInterfaces() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                MenuItem menuItem = new MenuItem(networkInterface.getName());
                menuItem.setOnAction(ignored -> menuButton.setText(menuItem.getText()));
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

    public void initCanvas(Canvas canvas) {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGrid(graphicsContext);
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(3);
        double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
        draw(graphicsContext, selfDevice, imageSize, SelfDevice.class);
        List<DeviceUI> gateways = deviceUiMapperService.findAll(Gateway.class);

        for (DeviceUI gateway : gateways) {
            draw(graphicsContext, gateway, imageSize, Gateway.class);
            drawConnection(graphicsContext, gateway, selfDevice);
            Gateway gatewayData = (Gateway) gateway.getDevice();
            List<DeviceUI> targets = deviceUiMapperService.findAll(Target.class).stream()
                    .filter(deviceUi -> gatewayData.getDevices().contains((Target) deviceUi.getDevice()))
                    .toList();
            for (DeviceUI target : targets) {
                draw(graphicsContext, target, imageSize, Target.class);
                drawConnection(graphicsContext, gateway, target);
            }
        }
    }

    public void calculateGatewaysPosition() {

        final List<DeviceUI> gateways = deviceUiMapperService.findAll(Gateway.class);
        int gatewaysSize = gateways.size();
        if (gatewaysSize == 0) return;
        double xCenter = deviceUiMapperService.getSelfDevice().getX();
        double yCenter = deviceUiMapperService.getSelfDevice().getY();
        double R = Math.min(canvas.getWidth(), canvas.getHeight()) / 3;
        double step = 2 * Math.PI / gatewaysSize;

        Iterator<DeviceUI> iterator = gateways.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            double angle = step * i;
            double x = xCenter + R * Math.cos(angle);
            double y = yCenter + R * Math.sin(angle);
            DeviceUI gateway = iterator.next();
            gateway.setX(x);
            gateway.setY(y);
            i++;
        }
    }

    private void centerSelfDevice() {
        DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
        selfDevice.setX(canvas.getWidth() / 2);
        selfDevice.setY(canvas.getHeight() / 2);

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            selfDevice.setX(canvas.getWidth() / 2);
            initCanvas(canvas);
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            selfDevice.setY(newVal.doubleValue() / 2);
            initCanvas(canvas);
        });
    }

    private void setupMouseEvents() {
        EventHandler<MouseEvent> onMousePressed = event -> {
            double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
            if (event.getButton() == MouseButton.SECONDARY) {
                BiPredicate<DeviceUI, MouseEvent> verifyClickInsideImage = (device, event1) -> {
                    double xLeftBorder = device.getX() - imageSize / 2;
                    double xRightBorder = device.getX() + imageSize / 2;
                    double yTopBorder = device.getY() - imageSize / 2;
                    double yBottomBorder = device.getY() + imageSize / 2;
                    double x = event1.getX();
                    double y = event1.getY();
                    return x <= xRightBorder && x >= xLeftBorder && y <= yBottomBorder && y >= yTopBorder;
                };

                DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
                if (verifyClickInsideImage.test(selfDevice, event)) {
                    selfDevice.getContextMenu().show(canvas, event.getScreenX(), event.getScreenY());
                    return;
                }

                for (DeviceUI device : deviceUiMapperService.getDevices()) {
                    if (verifyClickInsideImage.test(device, event)) {
                        device.getContextMenu().show(canvas, event.getScreenX(), event.getScreenY());
                        return;
                    }
                }
            } else {
                DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
                if (event.getX() >= selfDevice.getX() - imageSize / 2 && event.getX() <= selfDevice.getX() + imageSize / 2 &&
                        event.getY() >= selfDevice.getY() - imageSize / 2 && event.getY() <= selfDevice.getY() + imageSize / 2) {
                    draggingRouter = true;
                    dragOffsetX = event.getX() - selfDevice.getX();
                    dragOffsetY = event.getY() - selfDevice.getY();
                    return;
                }

                for (DeviceUI device : deviceUiMapperService.getDevices()) {
                    if (event.getX() >= device.getX() - imageSize / 2 && event.getX() <= device.getX() + imageSize / 2 &&
                            event.getY() >= device.getY() - imageSize / 2 && event.getY() <= device.getY() + imageSize / 2) {
                        draggedDevice = device;
                        dragOffsetX = event.getX() - device.getX();
                        dragOffsetY = event.getY() - device.getY();
                        break;
                    }
                }
            }
        };
        EventHandler<MouseEvent> onMouseDragged = event -> {
            DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
            if (draggingRouter) {
                double dx = event.getX() - dragOffsetX;
                double dy = event.getY() - dragOffsetY;
                if (dx < 0 || dx > canvas.getWidth() || dy > canvas.getHeight() || dy < 0)
                    return;
                selfDevice.setX(dx);
                selfDevice.setY(dy);
                PauseTransition pause = new PauseTransition(Duration.millis(10));
                pause.setOnFinished(e -> {
                    initCanvas(canvas);
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
                    initCanvas(canvas);
                });
                pause.play();
            }
        };
        canvas.setOnMousePressed(onMousePressed);
        canvas.setOnMouseDragged(onMouseDragged);
        canvas.setOnMouseReleased(ignored -> {
            draggedDevice = null;
            draggingRouter = false;
        });
    }

    private void drawConnection(GraphicsContext gc, DeviceUI startLine, DeviceUI endLine) {
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

        gc.setStroke(Color.RED);
        gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
    }

    private void draw(GraphicsContext gc, DeviceUI deviceUi, double imageSize, Class deviceClass) {
        if (deviceUi.getDevice() instanceof SelfDevice) {
            int enlargedSize = (int) (imageSize * 1.5);
            gc.drawImage(images.get(deviceClass),
                    deviceUi.getX() - enlargedSize / 2,
                    deviceUi.getY() - enlargedSize / 2,
                    enlargedSize,
                    enlargedSize);
        } else {
            gc.drawImage(images.get(deviceClass),
                    deviceUi.getX() - imageSize / 2,
                    deviceUi.getY() - imageSize / 2,
                    imageSize,
                    imageSize);
        }
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));
        gc.fillText(deviceUi.getDevice().getDeviceName(), deviceUi.getX() - 25, deviceUi.getY() + imageSize / 2 + 20);

        if (deviceClass.equals(SelfDevice.class)) {
            Optional<IpAddress> ipAddress = ((SelfDevice) deviceUi.getDevice()).findFirstIPv4();
            ipAddress.ifPresent(ipString -> {
                gc.fillText(ipString.getNetworkInterface(), deviceUi.getX() - 25, deviceUi.getY() + imageSize / 2 + 40);
                gc.fillText(ipString.getIp(), deviceUi.getX() - 25, deviceUi.getY() + imageSize / 2 + 60);
            });
        } else if (deviceClass.equals(Target.class)) {
            Target target = (Target) deviceUi.getDevice();
            gc.fillText(target.getNetworkInterface(), deviceUi.getX() - 25, deviceUi.getY() + imageSize / 2 + 40);
            target.findFirstIPv4().ifPresent(ipString -> gc.fillText(ipString, deviceUi.getX() - 25, deviceUi.getY() + imageSize / 2 + 60));
        } else {
            Gateway gateway = (Gateway) deviceUi.getDevice();
            gc.fillText(gateway.getNetworkInterface(), deviceUi.getX() - 25, deviceUi.getY() + imageSize / 2 + 40);
            gateway.findFirstIPv4().ifPresent(ipString -> gc.fillText(ipString, deviceUi.getX() - 25, deviceUi.getY() + imageSize / 2 + 60));
        }
    }

    private void drawGrid(GraphicsContext gc) {
        for (double percentage = 0.1; percentage <= 1.0; percentage += 0.1) {
            double x = canvas.getWidth() * percentage;
            double y = canvas.getHeight() * percentage;
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(1);
            gc.strokeLine(x, 0, x, canvas.getHeight());
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }
    }

}
