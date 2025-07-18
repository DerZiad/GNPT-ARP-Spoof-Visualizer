package org.npt.controllers;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.npt.models.*;
import org.npt.models.ui.DeviceUI;
import org.npt.models.ui.Frame;
import org.npt.uiservices.DeviceUiMapperService;
import org.npt.uiservices.FrameService;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@Slf4j
public class MainController extends DataInjector {

    private static DeviceUiMapperService deviceUiMapperService;
    private static final HashMap<Class<? extends Device>, Image> images = new HashMap<>();
    private DeviceUI draggedDevice = null;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean draggingRouter = false;
    private final long radarStartTime = System.currentTimeMillis();
    private double radarRadius = 0;
    private static final double RADAR_PERIOD_MS = 4000;
    private static final double MIN_DISTANCE_BETWEEN_DEVICES = 70;
    private static final double TEXT_LINE_HEIGHT = 18;
    private static final double TEXT_OFFSET_Y = 10;
    private static final double LABEL_WIDTH = 100;

    @FXML
    public VBox vboxPane;
    @FXML
    public Canvas canvas;
    @FXML
    public BorderPane borderPane;
    @FXML
    public MenuItem newMenu;
    @FXML
    public MenuItem addTargetMenu;
    @FXML
    public MenuItem aboutGnptMenu;

    @FXML
    public void initialize() {
        deviceUiMapperService = new DeviceUiMapperService(() -> drawNetwork(canvas), this::initDevices, canvas.getWidth(), canvas.getHeight());
        images.put(Target.class, new Image(graphicalNetworkTracerFactory.getResource("images/computer.png")));
        images.put(Gateway.class, new Image(graphicalNetworkTracerFactory.getResource("images/router.png")));
        images.put(SelfDevice.class, new Image(graphicalNetworkTracerFactory.getResource("images/hacker.png")));
        images.put(Interface.class, new Image(graphicalNetworkTracerFactory.getResource("images/interface.png")));
        canvas.widthProperty().bind(borderPane.widthProperty());
        canvas.heightProperty().bind(borderPane.heightProperty());
        borderPane.setMinSize(0, 0);
        canvas.widthProperty().addListener((ignored1, ignored2, ignored3) -> {
            resizeTargetsPositionAfterChangeEvent();
            calculateRootDevicePosition();
            calculateInterfaceAndGatewayPosition();
            drawNetwork(canvas);
        });
        canvas.heightProperty().addListener((ignored1, ignored2, ignored3) -> {
            resizeTargetsPositionAfterChangeEvent();
            calculateRootDevicePosition();
            calculateInterfaceAndGatewayPosition();
            drawNetwork(canvas);
        });
        newMenu.setOnAction(ignored -> {
            deviceUiMapperService.clear();
            calculateInterfaceAndGatewayPosition();
            drawNetwork(canvas);
        });

        addTargetMenu.setOnAction(e -> {
            final FrameService frameService = FrameService.getInstance();
            final Frame targetFrame = Frame.createAddTargetFrame();
            targetFrame.setArgs(new Object[]{deviceUiMapperService});
            final Stage stage = frameService.createNewStage(targetFrame, false, false);
            stage.setOnCloseRequest(ignored -> frameService.stopStage(targetFrame.getKey()));
        });
        setupMouseEvents();
        initDevices();
        final AnimationTimer radarTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = System.currentTimeMillis() - radarStartTime;
                radarRadius = (elapsed % RADAR_PERIOD_MS) / (double) RADAR_PERIOD_MS;
                drawNetwork(canvas);
            }
        };
        radarTimer.start();
    }

    private void initDevices() {
        calculateInterfaceAndGatewayPosition();
        calculateRootDevicePosition();
        drawNetwork(canvas);
    }

    private void calculateInterfaceAndGatewayPosition() {
        final List<DeviceUI> interfaces = deviceUiMapperService.findAll(Interface.class);
        final double baseRadius = Math.min(canvas.getWidth(), canvas.getHeight()) / 3;
        if (interfaces.isEmpty()) return;
        final double centerX = deviceUiMapperService.getSelfDevice().getX();
        final double centerY = deviceUiMapperService.getSelfDevice().getY();
        final double angleStep = 2 * Math.PI / interfaces.size();
        for (int i = 0; i < interfaces.size(); i++) {
            final double angle = i * angleStep;
            final double x = centerX + baseRadius * Math.cos(angle);
            final double y = centerY + baseRadius * Math.sin(angle);
            final Interface interfaceDevice = (Interface) interfaces.get(i).getDevice();
            if (interfaceDevice.getGatewayOptional().isPresent()) {
                final Gateway gatewayDataOptional = interfaceDevice.getGatewayOptional().get();
                final Optional<DeviceUI> gatewayUI = deviceUiMapperService
                        .getDevices()
                        .stream()
                        .filter(deviceUi -> deviceUi.getDevice().equals(gatewayDataOptional))
                        .findFirst();
                gatewayUI.ifPresent(gw -> {
                    final double routerX = centerX + baseRadius * 2 * Math.cos(angle);
                    final double routerY = centerY + baseRadius * 2 * Math.sin(angle);
                    gw.setX(routerX);
                    gw.setY(routerY);
                });
            }
            interfaces.get(i).setX(x);
            interfaces.get(i).setY(y);
        }
    }

    private void calculateRootDevicePosition() {
        final DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
        selfDevice.setX(canvas.getWidth() / 2);
        selfDevice.setY(canvas.getHeight() / 2);
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            selfDevice.setX(canvas.getWidth() / 2);
            drawNetwork(canvas);
        });
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            selfDevice.setY(newVal.doubleValue() / 2);
            drawNetwork(canvas);
        });
    }

    private void resizeTargetsPositionAfterChangeEvent() {
        final double newWidth = canvas.getWidth();
        final double newHeight = canvas.getHeight();
        if (canvas.getHeight() == 0 || canvas.getWidth() == 0)
            return;
        final List<DeviceUI> targets = deviceUiMapperService.findAll(Target.class);
        for (DeviceUI target : targets) {
            double xPercentage = target.getX() / deviceUiMapperService.getActualWidth();
            double yPercentage = target.getY() / deviceUiMapperService.getActualHeight();
            target.setX(xPercentage * newWidth);
            target.setY(yPercentage * newHeight);
        }
        deviceUiMapperService.setActualHeight(newHeight);
        deviceUiMapperService.setActualWidth(newWidth);
    }

    private void setupMouseEvents() {
        EventHandler<MouseEvent> onMousePressed = event -> {
            final double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
            BiPredicate<DeviceUI, MouseEvent> isInsideImage = (device, evt) -> {
                double x = evt.getX(), y = evt.getY();
                double left = device.getX() - imageSize / 2;
                double right = device.getX() + imageSize / 2;
                double top = device.getY() - imageSize / 2;
                double bottom = device.getY() + imageSize / 2;
                return x >= left && x <= right && y >= top && y <= bottom;
            };
            if (event.getButton() == MouseButton.SECONDARY) {
                DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
                if (isInsideImage.test(selfDevice, event)) {
                    selfDevice.getContextMenu().show(canvas, event.getScreenX(), event.getScreenY());
                    return;
                }
                for (DeviceUI device : deviceUiMapperService.getDevices()) {
                    if (isInsideImage.test(device, event)) {
                        device.getContextMenu().show(canvas, event.getScreenX(), event.getScreenY());
                        return;
                    }
                }
            } else {
                DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
                if (isInsideImage.test(selfDevice, event)) {
                    draggingRouter = true;
                    dragOffsetX = event.getX() - selfDevice.getX();
                    dragOffsetY = event.getY() - selfDevice.getY();
                    return;
                }
                for (DeviceUI device : deviceUiMapperService.getDevices()) {
                    if (isInsideImage.test(device, event)) {
                        draggedDevice = device;
                        dragOffsetX = event.getX() - device.getX();
                        dragOffsetY = event.getY() - device.getY();
                        break;
                    }
                }
            }
        };
        EventHandler<MouseEvent> onMouseDragged = event -> {
            final DeviceUI movingDevice = draggingRouter ? deviceUiMapperService.getSelfDevice() : draggedDevice;
            if (movingDevice == null) return;
            final double newX = event.getX() - dragOffsetX;
            final double newY = event.getY() - dragOffsetY;
            if (newX < 0 || newX > canvas.getWidth() || newY < 0 || newY > canvas.getHeight()) return;
            final DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
            boolean tooClose = deviceUiMapperService.getDevices().stream()
                    .filter(other -> other != movingDevice)
                    .anyMatch(other -> {
                        double dx = newX - other.getX();
                        double dy = newY - other.getY();
                        return Math.sqrt(dx * dx + dy * dy) < MIN_DISTANCE_BETWEEN_DEVICES;
                    });
            if (!draggingRouter && selfDevice != movingDevice) {
                double dx = newX - selfDevice.getX();
                double dy = newY - selfDevice.getY();
                if (Math.sqrt(dx * dx + dy * dy) < MIN_DISTANCE_BETWEEN_DEVICES) {
                    tooClose = true;
                }
            }
            if (tooClose) return;
            movingDevice.setX(newX);
            movingDevice.setY(newY);
            final PauseTransition pause = new PauseTransition(Duration.millis(10));
            pause.setOnFinished(e -> drawNetwork(canvas));
            pause.play();
        };
        canvas.setOnMousePressed(onMousePressed);
        canvas.setOnMouseDragged(onMouseDragged);
        canvas.setOnMouseReleased(event -> {
            draggedDevice = null;
            draggingRouter = false;
        });
    }

    public void drawNetwork(final Canvas canvas) {
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGrid(gc);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        final double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        final DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
        draw(gc, selfDevice, imageSize, SelfDevice.class);
        final List<DeviceUI> interfaces = deviceUiMapperService.findAll(Interface.class);
        final List<DeviceUI> targets = deviceUiMapperService.findAll(Target.class);
        for (DeviceUI interfaceUI : interfaces) {
            draw(gc, interfaceUI, imageSize, Interface.class);
            drawConnection(gc, interfaceUI, selfDevice);
            final Interface interfaceData = (Interface) interfaceUI.getDevice();
            final Optional<Gateway> gatewayDataOpt = interfaceData.getGatewayOptional();
            if (gatewayDataOpt.isEmpty()) continue;
            final DeviceUI gatewayUI = deviceUiMapperService.getDevices().stream()
                    .filter(deviceUi -> deviceUi.getDevice().equals(gatewayDataOpt.get()))
                    .findFirst().get();
            draw(gc, gatewayUI, imageSize, Gateway.class);
            drawConnection(gc, gatewayUI, interfaceUI);
            final List<DeviceUI> gwTargets = targets.stream()
                    .filter(deviceUi -> gatewayDataOpt.get().getDevices().contains((Target) deviceUi.getDevice()))
                    .toList();
            for (final DeviceUI target : gwTargets) {
                draw(gc, target, imageSize, Target.class);
                drawConnection(gc, gatewayUI, target);
            }
        }
        drawMinimap(gc);
    }

    private void drawMinimap(@NotNull GraphicsContext gc) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double mapSize = Math.min(width, height) * 0.25;
        double margin = 10;
        double mapX = width - mapSize - margin;
        double mapY = margin;
        gc.setFill(Color.rgb(240, 240, 240, 0.85));
        gc.fillRect(mapX, mapY, mapSize, mapSize);
        gc.setStroke(Color.GRAY);
        gc.strokeRect(mapX, mapY, mapSize, mapSize);
        gc.setStroke(Color.rgb(200, 200, 200, 0.7));
        gc.setLineWidth(1);
        int gridCount = 5;
        double gridStep = mapSize / gridCount;
        for (int i = 1; i < gridCount; i++) {
            double gx = mapX + i * gridStep;
            double gy = mapY + i * gridStep;
            gc.strokeLine(gx, mapY, gx, mapY + mapSize);
            gc.strokeLine(mapX, gy, mapX + mapSize, gy);
        }
        double radarCenterX = mapX + mapSize / 2;
        double radarCenterY = mapY + mapSize / 2;
        double maxRadarRadius = mapSize / 2;
        double currentRadarRadius = radarRadius * maxRadarRadius;
        if (currentRadarRadius > 0.5) {
            gc.setGlobalAlpha(0.6);
            gc.setStroke(Color.rgb(0, 180, 255, 0.6));
            gc.setLineWidth(3);
            gc.strokeOval(
                    radarCenterX - currentRadarRadius,
                    radarCenterY - currentRadarRadius,
                    currentRadarRadius * 2,
                    currentRadarRadius * 2
            );
            gc.setGlobalAlpha(1.0);
            gc.setLineWidth(1);
        }
        double scaleX = mapSize / width;
        double scaleY = mapSize / height;
        DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
        double selfX = mapX + selfDevice.getX() * scaleX;
        double selfY = mapY + selfDevice.getY() * scaleY;
        List<DeviceUI> interfaces = deviceUiMapperService.findAll(Interface.class);
        List<DeviceUI> targets = deviceUiMapperService.findAll(Target.class);
        gc.setStroke(Color.BLACK);
        for (DeviceUI interfaceUI : interfaces) {
            double ix = mapX + interfaceUI.getX() * scaleX;
            double iy = mapY + interfaceUI.getY() * scaleY;
            gc.strokeLine(ix, iy, selfX, selfY);
            Interface interfaceData = (Interface) interfaceUI.getDevice();
            Optional<Gateway> gatewayDataOpt = interfaceData.getGatewayOptional();
            if (gatewayDataOpt.isEmpty()) continue;
            DeviceUI gatewayUI = deviceUiMapperService.getDevices().stream()
                    .filter(deviceUi -> deviceUi.getDevice().equals(gatewayDataOpt.get()))
                    .findFirst().get();
            double gx = mapX + gatewayUI.getX() * scaleX;
            double gy = mapY + gatewayUI.getY() * scaleY;
            gc.strokeLine(gx, gy, ix, iy);
            List<DeviceUI> gwTargets = targets.stream()
                    .filter(deviceUi -> gatewayDataOpt.get().getDevices().contains((Target) deviceUi.getDevice()))
                    .toList();
            for (DeviceUI target : gwTargets) {
                double tx = mapX + target.getX() * scaleX;
                double ty = mapY + target.getY() * scaleY;
                gc.strokeLine(gx, gy, tx, ty);
            }
        }
        gc.setFill(Color.GREEN);
        gc.fillOval(selfX - 5, selfY - 5, 10, 10);
        for (DeviceUI interfaceUI : interfaces) {
            double ix = mapX + interfaceUI.getX() * scaleX;
            double iy = mapY + interfaceUI.getY() * scaleY;
            gc.setFill(Color.BLUE);
            gc.fillOval(ix - 5, iy - 5, 10, 10);
            Interface interfaceData = (Interface) interfaceUI.getDevice();
            Optional<Gateway> gatewayDataOpt = interfaceData.getGatewayOptional();
            if (gatewayDataOpt.isEmpty()) continue;
            DeviceUI gatewayUI = deviceUiMapperService.getDevices().stream()
                    .filter(deviceUi -> deviceUi.getDevice().equals(gatewayDataOpt.get()))
                    .findFirst().get();
            double gx = mapX + gatewayUI.getX() * scaleX;
            double gy = mapY + gatewayUI.getY() * scaleY;
            gc.setFill(Color.ORANGE);
            gc.fillOval(gx - 5, gy - 5, 10, 10);
            List<DeviceUI> gwTargets = targets.stream()
                    .filter(deviceUi -> gatewayDataOpt.get().getDevices().contains((Target) deviceUi.getDevice()))
                    .toList();
            for (DeviceUI target : gwTargets) {
                double tx = mapX + target.getX() * scaleX;
                double ty = mapY + target.getY() * scaleY;
                gc.setFill(Color.RED);
                gc.fillOval(tx - 5, ty - 5, 10, 10);
            }
        }
        for (DeviceUI target : targets) {
            boolean drawn = interfaces.stream().anyMatch(interfaceUI -> {
                Interface interfaceData = (Interface) interfaceUI.getDevice();
                Optional<Gateway> gatewayDataOpt = interfaceData.getGatewayOptional();
                return gatewayDataOpt.isPresent() && gatewayDataOpt.get().getDevices().contains((Target) target.getDevice());
            });
            if (!drawn) {
                double tx = mapX + target.getX() * scaleX;
                double ty = mapY + target.getY() * scaleY;
                gc.setFill(Color.RED);
                gc.fillOval(tx - 5, ty - 5, 10, 10);
            }
        }
        double legendBoxHeight = 4 * 22 + 16;
        double legendBoxX = mapX;
        double legendBoxY = mapY + mapSize + 8;
        gc.setFill(Color.rgb(240, 240, 240, 0.92));
        gc.fillRect(legendBoxX, legendBoxY, mapSize, legendBoxHeight);
        gc.setStroke(Color.GRAY);
        gc.strokeRect(legendBoxX, legendBoxY, mapSize, legendBoxHeight);
        double legendX = legendBoxX + 14;
        double legendY = legendBoxY + 14;
        double spacing = 22;
        gc.setFont(Font.font(14));
        gc.setFill(Color.GREEN);
        gc.fillOval(legendX, legendY, 14, 14);
        gc.setFill(Color.BLACK);
        gc.fillText("Self", legendX + 24, legendY + 12);
        gc.setFill(Color.BLUE);
        gc.fillOval(legendX, legendY + spacing, 14, 14);
        gc.setFill(Color.BLACK);
        gc.fillText("Interface", legendX + 24, legendY + spacing + 12);
        gc.setFill(Color.ORANGE);
        gc.fillOval(legendX, legendY + 2 * spacing, 14, 14);
        gc.setFill(Color.BLACK);
        gc.fillText("Gateway", legendX + 24, legendY + 2 * spacing + 12);
        gc.setFill(Color.RED);
        gc.fillOval(legendX, legendY + 3 * spacing, 14, 14);
        gc.setFill(Color.BLACK);
        gc.fillText("Target", legendX + 24, legendY + 3 * spacing + 12);
        gc.setGlobalAlpha(1.0);
        gc.setLineWidth(1);
    }

    private void drawConnection(GraphicsContext gc, DeviceUI startLine, DeviceUI endLine) {
        //final double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.135;
        //double r = Math.sqrt(imageSize * imageSize + imageSize * imageSize) / 2;
        final double r = 35;
        final double x1 = startLine.getX();
        final double y1 = startLine.getY();
        final double x2 = endLine.getX();
        final double y2 = endLine.getY();
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        final double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return;
        final double ux = dx / length;
        final double uy = dy / length;
        final double newX1 = x1 + ux * r;
        final double newY1 = y1 + uy * r;
        final double newX2 = x2 - ux * r;
        final double newY2 = y2 - uy * r;
        gc.setStroke(Color.BLACK);
        gc.strokeLine(newX1, newY1, newX2, newY2);
    }

    private <T> void draw(final GraphicsContext gc, final DeviceUI deviceUi, final double imageSize, final Class<T> deviceClass) {
        final double x = deviceUi.getX() - imageSize / 2;
        final double y = deviceUi.getY() - imageSize / 2;
        gc.drawImage(images.get(deviceClass), x, y, imageSize, imageSize);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));
        final double textX = deviceUi.getX() - LABEL_WIDTH / 2;
        final double baseY = y + imageSize + TEXT_OFFSET_Y;
        int line = 0;
        gc.fillText(deviceUi.getDevice().getDeviceName(), textX, baseY + TEXT_LINE_HEIGHT * line++);
        if (deviceClass.equals(Target.class)) {
            final Target target = (Target) deviceUi.getDevice();
            gc.fillText(target.getIp(), textX, baseY + TEXT_LINE_HEIGHT * line++);
        } else if (deviceClass.equals(Interface.class)) {
            final Interface intf = (Interface) deviceUi.getDevice();
            gc.fillText(intf.getIp(), textX, baseY + TEXT_LINE_HEIGHT * line++);
        } else if (deviceClass.equals(Gateway.class)) {
            final Gateway gateway = (Gateway) deviceUi.getDevice();
            gc.fillText(gateway.getIp(), textX, baseY + TEXT_LINE_HEIGHT * line++);
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
