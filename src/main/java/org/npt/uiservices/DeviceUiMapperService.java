package org.npt.uiservices;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.npt.controllers.FrameService;
import org.npt.exception.InvalidInputException;
import org.npt.exception.NotFoundException;
import org.npt.models.Gateway;
import org.npt.models.SelfDevice;
import org.npt.models.Target;
import org.npt.models.ui.DeviceUI;
import org.npt.models.ui.Frame;
import org.npt.services.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceUiMapperService {

    private static final GraphicalNetworkTracerFactory graphicalNetworkTracerFactory = GraphicalNetworkTracerFactory.getInstance();

    private static final DataService dataService = graphicalNetworkTracerFactory.getDataService();

    private static final TargetService targetService = graphicalNetworkTracerFactory.getTargetService();

    private static final ArpSpoofService arpSpoofService = graphicalNetworkTracerFactory.getArpSpoofService();

    private static final GatewayService gatewayService = graphicalNetworkTracerFactory.getGatewayService();

    private static final String START_SPOOFING_TEXT = "Start Spoofing";

    private static final String SHOW_DETAILS_TEXT = "View Details";

    private static final String REMOVE_DEVICE_TEXT = "Remove Device";

    private static final String SPY_TEXT = "Spy";

    private final Runnable refreshAction;

    @Getter
    private final DeviceUI selfDevice;

    @Getter
    private final List<DeviceUI> devices = new ArrayList<>();

    public DeviceUiMapperService(Runnable refreshAction) {
        this.refreshAction = refreshAction;
        selfDevice = new DeviceUI(dataService.getSelfDevice());
        initMenu(selfDevice);
        dataService.getDevices().forEach(device -> {
            DeviceUI deviceUI = new DeviceUI(device);
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

    public void addTarget(final String ipAddress, final String deviceInterface, final String deviceName) {
        try {
            Target target = targetService.create(deviceName, deviceInterface, new String[]{ipAddress});
            Optional<Gateway> gatewayOptional = gatewayService.find().stream()
                    .filter(gateway -> gateway.getNetworkInterface().equals(deviceInterface))
                    .findAny();
            gatewayOptional.ifPresent(associatedGateway -> associatedGateway.getDevices().add(target));
            DeviceUI deviceUI = new DeviceUI(target);
            initMenu(deviceUI);
            devices.add(deviceUI);
            refreshAction.run();
        } catch (InvalidInputException e) {
            ErrorHandler.handle(e);
        }
    }

    @SneakyThrows
    public void clear() {
        devices.clear();
        dataService.clear();
        arpSpoofService.clear();
        dataService.run();
        refreshAction.run();
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
            MenuItem startSpoofingMenuItem = getMenuItem(deviceUI, contextMenu);
            contextMenu.getItems().add(startSpoofingMenuItem);
        }
        contextMenu.getItems().addAll(detailsItem, removeItem);
    }

    private @NotNull MenuItem getMenuItem(DeviceUI deviceUI, ContextMenu contextMenu) {
        MenuItem startSpoofingMenuItem = new MenuItem(START_SPOOFING_TEXT);
        startSpoofingMenuItem.setOnAction(ignored -> {
            try {
                spoof(deviceUI);
                MenuItem menuItem = new MenuItem(SPY_TEXT);
                Frame statisticsFrame = Frame.createStatisticsDetails();
                statisticsFrame.setArgs(new Object[]{deviceUI.getDevice()});
                FrameService frameService = FrameService.getInstance();
                frameService.createNewScene(statisticsFrame, Frame.createMainFrame().getKey());
                contextMenu.getItems().add(menuItem);
                refreshAction.run();
            } catch (NotFoundException ex) {
                ErrorHandler.handle(ex);
            }
        });
        return startSpoofingMenuItem;
    }

    private void spoof(DeviceUI deviceUI) throws NotFoundException {
        Target target = (Target) deviceUI.getDevice();
        Gateway gateway = gatewayService.findByTarget(target)
                .orElseThrow(() -> new NotFoundException("Couldn't spoof a target that it is not connected to the same Network"));
        String scanInterface = target.getNetworkInterface();
        arpSpoofService.spoof(scanInterface, target, gateway);
    }

    public void showDetails(DeviceUI deviceUI) {
        FrameService frameService = FrameService.getInstance();
        switch (deviceUI.getDevice()) {
            case Target target -> {
                Frame detailsFrame = Frame.createTargetView();
                detailsFrame.setArgs(new Object[]{target, refreshAction});
                frameService.createNewStage(detailsFrame, true);
            }
            case SelfDevice selfDeviceObj -> {
                Frame detailsFrame = Frame.createSelfDetails();
                detailsFrame.setArgs(new Object[]{selfDeviceObj, refreshAction});
                frameService.createNewStage(detailsFrame, true);
            }
            case Gateway gateway -> {
                Frame detailsFrame = Frame.createGatewayDetails();
                detailsFrame.setArgs(new Object[]{gateway, refreshAction});
                frameService.createNewStage(detailsFrame, true);
            }
            default -> {
                // ignored
            }
        }
    }
}