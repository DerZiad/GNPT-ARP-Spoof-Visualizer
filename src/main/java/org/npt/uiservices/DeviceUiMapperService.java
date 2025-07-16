package org.npt.uiservices;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.npt.exception.DrawNetworkException;
import org.npt.exception.InvalidInputException;
import org.npt.exception.NotFoundException;
import org.npt.models.Gateway;
import org.npt.models.Interface;
import org.npt.models.SelfDevice;
import org.npt.models.Target;
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
        selfDevice = new DeviceUI(dataService.getSelfDevice());
        initMenu(selfDevice);
        dataService.getDevices().forEach(device -> {
            DeviceUI deviceUI = new DeviceUI(device);
            if (!(deviceUI.getDevice() instanceof Interface))
                initMenu(deviceUI);
            devices.add(deviceUI);
        });
    }

    public <T> List<DeviceUI> findAll(Class<T> clazz) {
        return devices.stream()
                .filter(Objects::nonNull)
                .filter(deviceUi -> clazz.isInstance(deviceUi.getDevice()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void addTarget(final String ipAddress, final String deviceInterface, final String deviceName) throws InvalidInputException {
        final Target target = dataService.createTarget(deviceName, deviceInterface, ipAddress);
        final DeviceUI deviceUI = new DeviceUI(target);
        initMenu(deviceUI);
        devices.add(deviceUI);
        refreshAction.run();
    }

    @SneakyThrows
    public void clear() {
        devices.clear();
        selfDevice = null;
        dataService.clear();
        arpSpoofService.clear();
        dataService.run();
        selfDevice = new DeviceUI(dataService.getSelfDevice());
        initMenu(selfDevice);
        dataService.getDevices().forEach(device -> {
            DeviceUI deviceUI = new DeviceUI(device);
            initMenu(deviceUI);
            devices.add(deviceUI);
        });
        hardRefreshAction.run();
    }

    public void refresh() {
        try {
            dataService.run();
        } catch (DrawNetworkException e) {
            ErrorHandler.handle(e);
        }
    }

    // Privates functions
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
                frameService.createNewStage(detailsFrame, false,false);
            }
            case SelfDevice ignored -> {
                Frame detailsFrame = Frame.createSelfDetails();
                detailsFrame.setArgs(new Object[]{refreshAction});
                frameService.createNewStage(detailsFrame, false,false);
            }
            case Gateway gateway -> {
                Frame detailsFrame = Frame.createGatewayDetails();
                detailsFrame.setArgs(new Object[]{gateway, refreshAction});
                frameService.createNewStage(detailsFrame, false,false);
            }
            default -> {
                // ignored
            }
        }
    }
}