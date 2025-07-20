package org.npt.controllers;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.npt.models.*;
import org.npt.models.ui.Frame;
import org.npt.uiservices.DeviceUiMapperService;
import org.npt.uiservices.FrameService;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
public class MainController extends DataInjector {

    private static DeviceUiMapperService deviceUiMapperService;
    private static final HashMap<Class<? extends Device>, Image> images = new HashMap<>();
    private Device draggedDevice = null;
    private double mouseDragOffsetX;
    private double mouseDragOffsetY;
    private final long radarStartTime = System.currentTimeMillis();
    private double radarRadius = 0;
    private static final double RADAR_PERIOD_MS = 4000;
    private static final double TEXT_LINE_HEIGHT = 18;
    private static final double TEXT_OFFSET_Y = 10;
    private static final double LABEL_WIDTH = 100;
    private double deviceImageSize;

    @FXML
    public Canvas canvas;
    @FXML
    public BorderPane borderPane;
    @FXML
    public MenuItem addTargetMenu;
    @FXML
    public MenuItem aboutGnptMenu;

    @FXML
    public void initialize() {
        deviceUiMapperService = new DeviceUiMapperService(() -> drawNetwork(canvas), canvas.getWidth(), canvas.getHeight());
        images.put(Target.class, new Image(graphicalNetworkTracerFactory.getResource("images/computer.png")));
        images.put(Gateway.class, new Image(graphicalNetworkTracerFactory.getResource("images/router.png")));
        images.put(SelfDevice.class, new Image(graphicalNetworkTracerFactory.getResource("images/hacker.png")));
        images.put(Interface.class, new Image(graphicalNetworkTracerFactory.getResource("images/interface.png")));
        canvas.widthProperty().bind(borderPane.widthProperty());
        canvas.heightProperty().bind(borderPane.heightProperty());
        borderPane.setMinSize(0, 0);
        updateImageSize();
        final SelfDevice selfDevice = deviceUiMapperService.getSelfDevice();
        canvas.widthProperty().addListener((ignored1, ignored2, ignored3) -> {
            updateImageSize();
            selfDevice.setX(canvas.getWidth() / 2);
            resizeDevicePositionsOnCanvasChange();
            drawNetwork(canvas);
        });
        canvas.heightProperty().addListener((ignored1, ignored2, newVal) -> {
            updateImageSize();
            selfDevice.setY(newVal.doubleValue() / 2);
            resizeDevicePositionsOnCanvasChange();
            drawNetwork(canvas);
        });
        addTargetMenu.setOnAction(ignored1 -> {
            final FrameService frameService = FrameService.getInstance();
            final Frame targetFrame = Frame.createAddTargetFrame();
            targetFrame.setArgs(new Object[]{deviceUiMapperService});
            final Stage stage = frameService.createNewStage(targetFrame, false, false);
            stage.setOnCloseRequest(ignored -> frameService.stopStage(targetFrame.getKey()));
        });
        setupMouseEvents();
        runNmapScanAndMapAnimation();
    }

    private void updateImageSize() {
        deviceImageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.08;
    }

    private void runNmapScanAndMapAnimation() {
        final Runnable rescan = () -> {
            final Task<Void> backgroundTask = new Task<>() {

                @Override
                protected Void call() throws Exception {
                    Platform.runLater(() -> {
                        deviceUiMapperService.rescan();
                    });
                    return null;
                }
            };
            new Thread(backgroundTask).start();
        };
        final AnimationTimer radarTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = System.currentTimeMillis() - radarStartTime;
                radarRadius = (elapsed % RADAR_PERIOD_MS) / RADAR_PERIOD_MS;
                drawNetwork(canvas);
            }
        };
        radarTimer.start();

        final long[] lastScanTime = {System.currentTimeMillis()};
        AnimationTimer nmapScanTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastScanTime[0] >= 5000) {
                    rescan.run();
                    lastScanTime[0] = currentTime;
                }
            }
        };
        nmapScanTimer.start();
    }

    private void initializeDevicePositions() {
        if (canvas.getHeight() == 0 || canvas.getWidth() == 0)
            return;
        final SelfDevice selfDevice = deviceUiMapperService.getSelfDevice();
        // Only set position if not already set
        if (!selfDevice.initialized()) {
            selfDevice.setX(canvas.getWidth() / 2);
            selfDevice.setY(canvas.getHeight() / 2);
        }

        final BiConsumer<List<? extends Device>, Pair<Double, Double>> drawCircle = (devices, position) -> {
            final List<? extends Device> toBeUpdated = devices.stream()
                    .filter(device -> !device.initialized())
                    .toList();
            if (toBeUpdated.isEmpty()) return;
            final double baseRadius = Math.min(canvas.getWidth(), canvas.getHeight()) / 5.0;
            final double centerX = position.getKey();
            final double centerY = position.getValue();
            final double angleStep = 2 * Math.PI / toBeUpdated.size();
            for (int i = 0; i < toBeUpdated.size(); i++) {
                final double angle = i * angleStep;
                final double x = centerX + baseRadius * Math.cos(angle);
                final double y = centerY + baseRadius * Math.sin(angle);
                final Device device = toBeUpdated.get(i);
                device.setX(x);
                device.setY(y);
            }
        };
        drawCircle.accept(selfDevice.getAnInterfaces(), new Pair<>(selfDevice.getX(), selfDevice.getY()));
        selfDevice.getAnInterfaces().stream()
                .filter(anInterface -> anInterface.getGateway() != null)
                .forEach(anInterface -> {
                    drawCircle.accept(Arrays.asList(anInterface.getGateway()), new Pair<>(anInterface.getX(), anInterface.getY()));
                    drawCircle.accept(anInterface.getGateway().getDevices(), new Pair<>(anInterface.getGateway().getX(), anInterface.getGateway().getY()));
                });
    }

    private void resizeDevicePositionsOnCanvasChange() {
        final double prevWidth = deviceUiMapperService.getActualWidth();
        final double prevHeight = deviceUiMapperService.getActualHeight();
        final double newWidth = canvas.getWidth();
        final double newHeight = canvas.getHeight();
        if (newWidth != 0 && newHeight != 0 && prevWidth != 0 && prevHeight != 0) {
            forEachDevice(device -> {
                if (device.initialized()) {
                    double xRatio = device.getX() / prevWidth;
                    double yRatio = device.getY() / prevHeight;
                    device.setX(xRatio * newWidth);
                    device.setY(yRatio * newHeight);
                }
            }, false);
        }
        deviceUiMapperService.setActualHeight(newHeight);
        deviceUiMapperService.setActualWidth(newWidth);
    }

    private void setupMouseEvents() {
        EventHandler<MouseEvent> handleMousePressed = event -> {
            // check and close or context menu if it is open
            final Collection<ContextMenu> contextMenus = deviceUiMapperService.getContextMenus().values();
            contextMenus.forEach(ContextMenu::hide);
            BiPredicate<Device, MouseEvent> isInsideImage = (device, evt) -> {
                // Null check for device and its coordinates
                if (device == null || device.getX() == null || device.getY() == null) return false;
                double x = evt.getX(), y = evt.getY();
                double left = device.getX() - deviceImageSize / 2;
                double right = device.getX() + deviceImageSize / 2;
                double top = device.getY() - deviceImageSize / 2;
                double bottom = device.getY() + deviceImageSize / 2;
                return x >= left && x <= right && y >= top && y <= bottom;
            };
            if (event.getButton() == MouseButton.SECONDARY) {
                forEachDevice(device -> {
                    if (isInsideImage.test(device, event)) {
                        deviceUiMapperService.getContextMenu(device).show(canvas, event.getScreenX(), event.getScreenY());
                    }
                }, true);
                return;
            } else {
                forEachDevice(device -> {
                    if (isInsideImage.test(device, event)) {
                        draggedDevice = device;
                        mouseDragOffsetX = event.getX() - device.getX();
                        mouseDragOffsetY = event.getY() - device.getY();
                        return;
                    }
                }, true);
            }
        };
        EventHandler<MouseEvent> handleMouseDragged = event -> {
            if (draggedDevice == null || !draggedDevice.initialized()) return;
            final double newX = event.getX() - mouseDragOffsetX;
            final double newY = event.getY() - mouseDragOffsetY;
            if (newX < 0 || newX > canvas.getWidth() || newY < 0 || newY > canvas.getHeight()) return;
            double borderRadius = Math.sqrt(deviceImageSize * deviceImageSize + deviceImageSize * deviceImageSize) / 2;

            // Check if draggedDevice is too close to any other initialized device (including selfDevice if needed)
            Predicate<Device> tooClose = other -> {
                if (other == draggedDevice || !other.initialized()) return false;
                double dx = newX - other.getX();
                double dy = newY - other.getY();
                return Math.sqrt(dx * dx + dy * dy) < borderRadius * 2;
            };

            // Check if draggedDevice is too close to selfDevice
            if (!draggedDevice.equals(deviceUiMapperService.getSelfDevice())) {
                if (tooClose.test(deviceUiMapperService.getSelfDevice()))
                    return;
            }

            // Check if draggedDevice is too close to any Interface, Gateway, or Target
            for (Interface anInterface : deviceUiMapperService.getSelfDevice().getAnInterfaces()) {
                if (tooClose.test(anInterface)) return;
                if (anInterface.getGateway() != null) {
                    Gateway gateway = anInterface.getGateway();
                    if (tooClose.test(gateway)) return;
                    for (Target target : gateway.getDevices()) {
                        if (tooClose.test(target)) return;
                    }
                }
            }

            draggedDevice.setX(newX);
            draggedDevice.setY(newY);
            final PauseTransition pause = new PauseTransition(Duration.millis(10));
            pause.setOnFinished(e -> drawNetwork(canvas));
            pause.play();
        };
        canvas.setOnMousePressed(handleMousePressed);
        canvas.setOnMouseDragged(handleMouseDragged);
        canvas.setOnMouseReleased(event -> {
            draggedDevice = null;
        });
    }

    public void drawNetwork(final Canvas canvas) {
        initializeDevicePositions();
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawCanvasGrid(gc);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        final SelfDevice selfDevice = deviceUiMapperService.getSelfDevice();
        drawDeviceComponent(gc, selfDevice, deviceImageSize, SelfDevice.class);
        for (final Interface anInterface : selfDevice.getAnInterfaces()) {
            drawDeviceComponent(gc, anInterface, deviceImageSize, Interface.class);
            drawDeviceConnection(gc, anInterface, selfDevice);
            if (anInterface.getGateway() == null) continue;
            final Gateway gateway = anInterface.getGateway();
            drawDeviceComponent(gc, gateway, deviceImageSize, Gateway.class);
            drawDeviceConnection(gc, gateway, anInterface);
            for (final Target target : gateway.getDevices()) {
                drawDeviceComponent(gc, target, deviceImageSize, Target.class);
                drawDeviceConnection(gc, gateway, target);
            }
        }
        drawMiniMap(gc);
    }

    private void drawMiniMap(final @NotNull GraphicsContext gc) {
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

        drawCanvasGrid(gc, mapX, mapY, mapSize, mapSize, 0.1);

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
        final SelfDevice selfDevice = deviceUiMapperService.getSelfDevice();
        // Only draw if initialized
        if (selfDevice.initialized()) {
            double selfX = mapX + selfDevice.getX() * scaleX;
            double selfY = mapY + selfDevice.getY() * scaleY;
            final List<Interface> interfaces = selfDevice.getAnInterfaces();
            gc.setStroke(Color.BLACK);
            for (Interface anInterface : interfaces) {
                if (!anInterface.initialized()) continue;
                double ix = mapX + anInterface.getX() * scaleX;
                double iy = mapY + anInterface.getY() * scaleY;
                gc.strokeLine(ix, iy, selfX, selfY);
                if (anInterface.getGateway() == null || !anInterface.getGateway().initialized()) continue;
                Gateway gateway = anInterface.getGateway();
                double gx = mapX + gateway.getX() * scaleX;
                double gy = mapY + gateway.getY() * scaleY;
                gc.strokeLine(gx, gy, ix, iy);
                for (Target target : gateway.getDevices()) {
                    if (!target.initialized()) continue;
                    double tx = mapX + target.getX() * scaleX;
                    double ty = mapY + target.getY() * scaleY;
                    gc.strokeLine(gx, gy, tx, ty);
                }
            }
            gc.setFill(Color.GREEN);
            gc.fillOval(selfX - 5, selfY - 5, 10, 10);
            for (Interface interfaceUI : interfaces) {
                if (!interfaceUI.initialized()) continue;
                double ix = mapX + interfaceUI.getX() * scaleX;
                double iy = mapY + interfaceUI.getY() * scaleY;
                gc.setFill(Color.BLUE);
                gc.fillOval(ix - 5, iy - 5, 10, 10);
                if (interfaceUI.getGateway() == null || !interfaceUI.getGateway().initialized()) continue;
                Gateway gatewayUI = interfaceUI.getGateway();
                double gx = mapX + gatewayUI.getX() * scaleX;
                double gy = mapY + gatewayUI.getY() * scaleY;
                gc.setFill(Color.ORANGE);
                gc.fillOval(gx - 5, gy - 5, 10, 10);
                for (Target target : gatewayUI.getDevices()) {
                    if (!target.initialized()) continue;
                    double tx = mapX + target.getX() * scaleX;
                    double ty = mapY + target.getY() * scaleY;
                    gc.setFill(Color.RED);
                    gc.fillOval(tx - 5, ty - 5, 10, 10);
                }
            }
        }
        List<Target> allTargets = deviceUiMapperService.getDevicesByType(Target.class);
        for (Target target : allTargets) {
            if (!target.initialized()) continue;
            boolean drawn = selfDevice.getAnInterfaces().stream().anyMatch(anInterface -> anInterface.targetAlreadyScanned(target));
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

    private void drawDeviceConnection(final GraphicsContext gc, final Device startDevice, final Device endDevice) {
        if (!startDevice.initialized() || !endDevice.initialized())
            return;
        double r = Math.sqrt(deviceImageSize * deviceImageSize + deviceImageSize * deviceImageSize) / 2;
        final double x1 = startDevice.getX();
        final double y1 = startDevice.getY();
        final double x2 = endDevice.getX();
        final double y2 = endDevice.getY();
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
        if (Math.abs(newX1 - newX2) < 1 && Math.abs(newY1 - newY2) < 1) return;
        gc.setStroke(Color.BLACK);
        gc.strokeLine(newX1, newY1, newX2, newY2);
    }

    private <T> void drawDeviceComponent(final GraphicsContext gc, final Device device, final double deviceImageSize, final Class<T> deviceClass) {
        if (!device.initialized())
            return;
        final double x = device.getX() - deviceImageSize / 2;
        final double y = device.getY() - deviceImageSize / 2;
        gc.drawImage(images.get(deviceClass), x, y, deviceImageSize, deviceImageSize);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));
        final double textX = device.getX() - LABEL_WIDTH / 2;
        final double baseY = y + deviceImageSize + TEXT_OFFSET_Y;
        int line = 0;
        gc.fillText(device.getDeviceName(), textX, baseY + TEXT_LINE_HEIGHT * line++);
        switch (device) {
            case Target target -> {
                gc.fillText(target.getIp(), textX, baseY + TEXT_LINE_HEIGHT * line++);
            }

            case Interface intf -> {
                gc.fillText(intf.getIp(), textX, baseY + TEXT_LINE_HEIGHT * line++);
            }

            case Gateway gateway -> {
                gc.fillText(gateway.getIp(), textX, baseY + TEXT_LINE_HEIGHT * line++);
            }

            default -> {

            }
        }
    }

    private void drawCanvasGrid(GraphicsContext gc) {
        drawCanvasGrid(gc, 0, 0, canvas.getWidth(), canvas.getHeight(), 0.1);
    }

    private void drawCanvasGrid(GraphicsContext gc, double x0, double y0, double width, double height, double stepFraction) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (double percentage = stepFraction; percentage <= 1.0; percentage += stepFraction) {
            double x = x0 + width * percentage;
            double y = y0 + height * percentage;
            gc.strokeLine(x, y0, x, y0 + height);
            gc.strokeLine(x0, y, x0 + width, y);
        }
    }

    private void forEachDevice(Consumer<Device> action, boolean includeSelfDevice) {
        if (includeSelfDevice) {
            action.accept(deviceUiMapperService.getSelfDevice());
        }
        for (Interface anInterface : deviceUiMapperService.getSelfDevice().getAnInterfaces()) {
            action.accept(anInterface);
            if (anInterface.getGateway() != null) {
                action.accept(anInterface.getGateway());
                for (Target target : anInterface.getGateway().getDevices()) {
                    action.accept(target);
                }
            }
        }
    }
}
