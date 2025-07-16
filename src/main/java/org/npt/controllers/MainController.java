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
import org.npt.models.ui.Frame;
import org.npt.uiservices.DeviceUiMapperService;
import org.npt.uiservices.FrameService;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
    public Canvas canvas;

    @FXML
    public MenuButton menuButton;

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
            menuButton.getItems().clear();
            deviceUiMapperService.clear();
        });

        refresh.setOnAction(e -> {
            menuButton.getItems().clear();
            deviceUiMapperService.refresh();
            calculateInterfaceAndGatewayPosition();
            drawNetwork(canvas);
        });

        addTargetMenu.setOnAction(e -> {
            final FrameService frameService = FrameService.getInstance();
            final Frame targetFrame = Frame.createAddTargetFrame();
            targetFrame.setArgs(new Object[]{deviceUiMapperService});
            frameService.createNewStage(targetFrame, false,false);
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
                pause.setOnFinished(e -> drawNetwork(canvas));
                pause.play();
            } else if (draggedDevice != null) {
                double dx = event.getX() - dragOffsetX;
                double dy = event.getY() - dragOffsetY;
                if (dx < 0 || dx > canvas.getWidth() || dy > canvas.getHeight() || dy < 0)
                    return;
                draggedDevice.setX(dx);
                draggedDevice.setY(dy);
                PauseTransition pause = new PauseTransition(Duration.millis(10));
                pause.setOnFinished(e -> drawNetwork(canvas));
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

        if (x1 == x2) {
            double dy = (y2 > y1 ? 1 : -1) * r;
            gc.strokeLine(x1, y1 + dy, x2, y2);
            return;
        }

        final double a = (y2 - y1) / (x2 - x1);
        final double b = y2 - a * x2;

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

        gc.setStroke(Color.BLACK);
        gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
    }

    private static final double TEXT_LINE_HEIGHT = 18;
    private static final double TEXT_OFFSET_Y = 10;
    private static final double LABEL_WIDTH = 100;

    private <T> void draw(GraphicsContext gc, DeviceUI deviceUi, double imageSize, Class<T> deviceClass) {
        double x = deviceUi.getX() - imageSize / 2;
        double y = deviceUi.getY() - imageSize / 2;

        // Draw only the device image (no border)
        gc.drawImage(images.get(deviceClass), x, y, imageSize, imageSize);

        // Prepare to draw text
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));

        // Display lines of text below the icon
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
