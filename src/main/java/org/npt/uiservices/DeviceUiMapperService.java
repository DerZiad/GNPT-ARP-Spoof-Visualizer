package org.npt.uiservices;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.npt.exception.InvalidInputException;
import org.npt.exception.NotFoundException;
import org.npt.models.*;
import org.npt.models.ui.DeviceUI;
import org.npt.models.ui.Frame;
import org.npt.services.ArpSpoofService;
import org.npt.services.DataService;
import org.npt.services.GraphicalNetworkTracerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DeviceUiMapperService {

    private static final GraphicalNetworkTracerFactory graphicalNetworkTracerFactory = GraphicalNetworkTracerFactory.getInstance();

    private static final DataService dataService = graphicalNetworkTracerFactory.getDataService();

    private static final ArpSpoofService arpSpoofService = graphicalNetworkTracerFactory.getArpSpoofService();

    private static final FrameService frameService = FrameService.getInstance();

    private static final String START_SPOOFING_TEXT = "Start Spoofing";

    private static final String SHOW_DETAILS_TEXT = "View Details";

    private static final String REMOVE_DEVICE_TEXT = "Remove Device";

    private final Runnable refreshAction;
    private final Runnable hardRefreshAction;

    @Getter
    private DeviceUI selfDevice;

    @Getter
    private final List<DeviceUI> devices = new ArrayList<>();

    @Getter
    @Setter
    private double actualWidth;

    @Getter
    @Setter
    private double actualHeight;

    public DeviceUiMapperService(Runnable refreshAction, Runnable hardRefreshAction, double actualWidth, double actualHeight) {
        this.refreshAction = refreshAction;
        this.hardRefreshAction = hardRefreshAction;
        this.actualHeight = actualHeight;
        this.actualWidth = actualWidth;
        selfDevice = initUIDevices(dataService.getSelfDevice());
    }

    public <T> List<DeviceUI> findAll(Class<T> clazz) {
        return devices.stream()
                .filter(Objects::nonNull)
                .filter(deviceUi -> clazz.isInstance(deviceUi.getDevice()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void addTarget(final String ipAddress, final String deviceInterface, final String deviceName) throws InvalidInputException {
        final Target target = dataService.createTarget(deviceName, deviceInterface, ipAddress);
        final DeviceUI targetUI = createDeviceUI(target);
        for (final DeviceUI interfaceUI : selfDevice.getChildren()) {
            if (interfaceUI.getDevice().getDeviceName().equals(deviceInterface)) {
                final DeviceUI gatewayUI = interfaceUI.getChildren().getFirst();
                gatewayUI.getChildren().add(targetUI);
                refreshAction.run();
                return;
            }
        }

    }

    @SneakyThrows
    public void clear() {
        devices.clear();
        selfDevice = null;
        dataService.clear();
        arpSpoofService.clear();
        dataService.run();
        selfDevice = initUIDevices(dataService.getSelfDevice());
        hardRefreshAction.run();
    }

    // Privates functions

    private DeviceUI initUIDevices(SelfDevice selfDevice) {
        final DeviceUI selfDeviceUI = createDeviceUI(selfDevice);
        for (final Interface interfaceData : selfDevice.getAnInterfaces()) {
            final DeviceUI interfaceUI = createDeviceUI(interfaceData);
            selfDeviceUI.getChildren().add(interfaceUI);
            if (interfaceData.getGatewayOptional().isPresent()) {
                final Gateway gateway = interfaceData.getGatewayOptional().get();
                final DeviceUI gatewayUI = createDeviceUI(gateway);
                interfaceUI.getChildren().add(gatewayUI);
                final List<Target> targets = gateway.getDevices();
                for (Target target : targets) {
                    final DeviceUI targetUI = createDeviceUI(target);
                    gatewayUI.getChildren().add(targetUI);
                }
            }
        }
        return selfDeviceUI;
    }

    private DeviceUI createDeviceUI(Device device) {
        final DeviceUI deviceUI = new DeviceUI(device);
        if (!(device instanceof Interface))
            initMenu(deviceUI);
        this.devices.add(deviceUI);
        return deviceUI;
    }

    private void initMenu(DeviceUI deviceUI) {
        ContextMenu contextMenu = deviceUI.getContextMenu();
        MenuItem detailsItem = new MenuItem(SHOW_DETAILS_TEXT);
        detailsItem.setOnAction(ignored -> showDetails(deviceUI));

        MenuItem removeItem = new MenuItem(REMOVE_DEVICE_TEXT);
        removeItem.setOnAction(ignored -> {
            devices.remove(deviceUI);
            dataService.removeByObject(deviceUI.getDevice());
            refreshAction.run();
        });

        if (deviceUI.getDevice() instanceof Target) {
            MenuItem startSpoofingMenuItem = getMenuItem(deviceUI);
            contextMenu.getItems().add(startSpoofingMenuItem);
        }
        contextMenu.getItems().addAll(detailsItem, removeItem);
    }

    private @NotNull MenuItem getMenuItem(DeviceUI deviceUI) {
        MenuItem startSpoofingMenuItem = new MenuItem(START_SPOOFING_TEXT);
        startSpoofingMenuItem.setOnAction(ignored -> {
            try {
                spoof(deviceUI);
                Frame statisticsFrame = Frame.createStatisticsDetails();
                statisticsFrame.setArgs(new Object[]{deviceUI.getDevice()});
                frameService.createNewScene(statisticsFrame, Frame.createMainFrame().getKey());
                refreshAction.run();
            } catch (NotFoundException ex) {
                ErrorHandler.handle(ex);
            }
        });
        return startSpoofingMenuItem;
    }

    private void spoof(DeviceUI deviceUI) throws NotFoundException {
        Target target = (Target) deviceUI.getDevice();
        Gateway gateway = dataService.findGatewayByTarget(target)
                .orElseThrow(() -> new NotFoundException("Couldn't spoof a target that it is not connected to the same Network"));
        String scanInterface = dataService.getSelfDevice()
                .getAnInterfaces()
                .stream()
                .filter(anInterface -> anInterface.getGatewayOptional().isPresent())
                .filter(anInterface -> anInterface.getGatewayOptional().get().equals(gateway))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No interface found for the gateway"))
                .getDeviceName();
        arpSpoofService.spoof(scanInterface, target, gateway);
    }

    private void showDetails(DeviceUI deviceUI) {
        switch (deviceUI.getDevice()) {
            case Target target -> {
                Frame detailsFrame = Frame.createTargetView();
                detailsFrame.setArgs(new Object[]{target, refreshAction});
                final Stage stage = frameService.createNewStage(detailsFrame, false, false);
                handlePopupClose(stage, detailsFrame);
            }
            case SelfDevice ignored -> {
                Frame detailsFrame = Frame.createSelfDetails();
                detailsFrame.setArgs(new Object[]{refreshAction});
                final Stage stage = frameService.createNewStage(detailsFrame, false, false);
                handlePopupClose(stage, detailsFrame);
            }
            case Gateway gateway -> {
                final Frame detailsFrame = Frame.createGatewayDetails();
                detailsFrame.setArgs(new Object[]{gateway, refreshAction});
                final Stage stage = frameService.createNewStage(detailsFrame, false, false);
                handlePopupClose(stage, detailsFrame);
            }
            default -> {
                // ignored
            }
        }
    }

    private void handlePopupClose(final Stage stage, Frame frame) {
        stage.setOnCloseRequest(ignored -> frameService.stopStage(frame.getKey()));
    }
}