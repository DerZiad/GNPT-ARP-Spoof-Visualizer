package org.npt.controllers;

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

    @FXML
    public VBox vboxPane;

    @FXML
    public Canvas canvas;

    @FXML
    public BorderPane borderPane;

    @FXML
    public MenuItem newMenu;

    @FXML
    public MenuItem refresh;

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

        refresh.setOnAction(e -> {
            deviceUiMapperService.refresh();
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
    }

    private void initDevices() {
        calculateInterfaceAndGatewayPosition();
        calculateRootDevicePosition();
        drawNetwork(canvas);
    }

    private void calculateInterfaceAndGatewayPosition() {
        List<DeviceUI> interfaces = deviceUiMapperService.findAll(Interface.class);
        double baseRadius = Math.min(canvas.getWidth(), canvas.getHeight()) / 3;
        if (interfaces.isEmpty()) return;

        double centerX = deviceUiMapperService.getSelfDevice().getX();
        double centerY = deviceUiMapperService.getSelfDevice().getY();
        double angleStep = 2 * Math.PI / interfaces.size();

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
        DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
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
        double newWidth = canvas.getWidth();
        double newHeight = canvas.getHeight();
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

    private static final double MIN_DISTANCE_BETWEEN_DEVICES = 70;

    private void setupMouseEvents() {
        EventHandler<MouseEvent> onMousePressed = event -> {
            double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;

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
            DeviceUI movingDevice = draggingRouter ? deviceUiMapperService.getSelfDevice() : draggedDevice;
            if (movingDevice == null) return;

            double newX = event.getX() - dragOffsetX;
            double newY = event.getY() - dragOffsetY;

            if (newX < 0 || newX > canvas.getWidth() || newY < 0 || newY > canvas.getHeight()) return;

            DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();

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

            PauseTransition pause = new PauseTransition(Duration.millis(10));
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

    public void drawNetwork(Canvas canvas) {
        final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGrid(graphicsContext);
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(3);
        final double imageSize = Math.min(canvas.getWidth(), canvas.getHeight()) * 0.1;
        final DeviceUI selfDevice = deviceUiMapperService.getSelfDevice();
        draw(graphicsContext, selfDevice, imageSize, SelfDevice.class);
        final List<DeviceUI> interfaces = deviceUiMapperService.findAll(Interface.class);
        for (final DeviceUI interfaceUI : interfaces) {
            draw(graphicsContext, interfaceUI, imageSize, Interface.class);
            drawConnection(graphicsContext, interfaceUI, selfDevice);
            final Interface interfaceData = (Interface) interfaceUI.getDevice();
            final Optional<Gateway> gatewayDataOpt = interfaceData.getGatewayOptional();
            if (gatewayDataOpt.isEmpty())
                continue;
            final DeviceUI gatewayUI = deviceUiMapperService
                    .getDevices()
                    .stream()
                    .filter(deviceUi -> deviceUi.getDevice().equals(gatewayDataOpt.get()))
                    .findFirst()
                    .get();
            draw(graphicsContext, gatewayUI, imageSize, Gateway.class);
            drawConnection(graphicsContext, gatewayUI, interfaceUI);
            final List<DeviceUI> targets = deviceUiMapperService.findAll(Target.class).stream()
                    .filter(deviceUi -> gatewayDataOpt.get().getDevices().contains((Target) deviceUi.getDevice()))
                    .toList();
            for (DeviceUI target : targets) {
                draw(graphicsContext, target, imageSize, Target.class);
                drawConnection(graphicsContext, gatewayUI, target);
            }

        }
    }

    private void drawConnection(GraphicsContext gc, DeviceUI startLine, DeviceUI endLine) {
        final double r = 35;
        final double x1 = startLine.getX();
        final double y1 = startLine.getY();
        final double x2 = endLine.getX();
        final double y2 = endLine.getY();

        double dx = x2 - x1;
        double dy = y2 - y1;

        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return;

        double ux = dx / length;
        double uy = dy / length;

        double newX1 = x1 + ux * r;
        double newY1 = y1 + uy * r;
        double newX2 = x2 - ux * r;
        double newY2 = y2 - uy * r;

        gc.setStroke(Color.BLACK);
        gc.strokeLine(newX1, newY1, newX2, newY2);
    }

    private static final double TEXT_LINE_HEIGHT = 18;
    private static final double TEXT_OFFSET_Y = 10;
    private static final double LABEL_WIDTH = 100;

    private <T> void draw(GraphicsContext gc, DeviceUI deviceUi, double imageSize, Class<T> deviceClass) {
        double x = deviceUi.getX() - imageSize / 2;
        double y = deviceUi.getY() - imageSize / 2;

        gc.drawImage(images.get(deviceClass), x, y, imageSize, imageSize);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));

        double textX = deviceUi.getX() - LABEL_WIDTH / 2;
        double baseY = y + imageSize + TEXT_OFFSET_Y;
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