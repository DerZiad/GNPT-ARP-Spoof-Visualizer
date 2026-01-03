package org.npt.uiservices;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.npt.exception.DrawNetworkException;
import org.npt.exception.InvalidInputException;
import org.npt.exception.NotFoundException;
import org.npt.models.*;
import org.npt.models.ui.Frame;
import org.npt.services.ArpSpoofService;
import org.npt.services.DataService;
import org.npt.services.GraphicalNetworkTracerFactory;

import java.util.Collections;
import java.util.HashMap;
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

    @Getter
    private static final HashMap<String, ContextMenu> contextMenus = new HashMap<>();

    private final Runnable refreshAction;

    @Getter
    @Setter
    private double actualWidth;

    @Getter
    @Setter
    private double actualHeight;

    public DeviceUiMapperService(Runnable refreshAction, double actualWidth, double actualHeight) {
        this.refreshAction = refreshAction;
        this.actualHeight = actualHeight;
        this.actualWidth = actualWidth;
        configureDevices();
    }

    public void addTarget(final String ipAddress, final String deviceInterface, final String deviceName) throws InvalidInputException {
        final Target target = dataService.createTarget(deviceName, deviceInterface, ipAddress);
        initMenu(target);
        refreshAction.run();
    }

    public void configureDevices() {
        initMenu(dataService.getSelfDevice());
        for (Interface anInterface : dataService.getSelfDevice().getAnInterfaces()) {
            final Gateway gateway = anInterface.getGateway();
            if (gateway != null) {
                initMenu(gateway);
                gateway.getDevices().forEach(this::initMenu);
            }
        }
    }

    public <T extends Device> List<T> getDevicesByType(@NotNull Class<T> type) {
        final List<Interface> interfaces = dataService.getSelfDevice().getAnInterfaces();

        if (type.equals(Interface.class)) {
            return (List<T>) interfaces;
        }

        if (type.equals(Gateway.class)) {
            return interfaces.stream()
                    .map(Interface::getGateway)
                    .filter(Objects::nonNull)
                    .filter(type::isInstance)
                    .map(type::cast)
                    .collect(Collectors.toList());
        }

        if (type.equals(Target.class)) {
            return interfaces.stream()
                    .map(Interface::getGateway)
                    .filter(Objects::nonNull)
                    .flatMap(gateway -> gateway.getDevices().stream())
                    .map(type::cast)
                    .collect(Collectors.toList());
        }

        // Return empty list for unsupported types
        return Collections.emptyList();
    }

    public ContextMenu getContextMenu(Device device) {
        return contextMenus.getOrDefault(device.getKey(),initMenu(device));
    }

    public SelfDevice getSelfDevice() {
        return dataService.getSelfDevice();
    }

    // Privates functions

    private ContextMenu initMenu(final Device device) {
        final ContextMenu contextMenu = new ContextMenu();
        final List<MenuItem> menuItems = contextMenu.getItems();
        final MenuItem detailsItem = new MenuItem(SHOW_DETAILS_TEXT);
        detailsItem.setOnAction(ignored -> showDetails(device));
        menuItems.add(detailsItem);

        if (device instanceof Target target) {
            final MenuItem removeItem = new MenuItem(REMOVE_DEVICE_TEXT);
            removeItem.setOnAction(ignored -> {
                contextMenus.remove(target.getKey());
                dataService.remove(target);
                refreshAction.run();
            });

            final MenuItem startSpoofingMenuItem = getTargetMenuItem(target);
            menuItems.add(startSpoofingMenuItem);
            menuItems.add(removeItem);
        }
        contextMenus.put(device.getKey(), contextMenu);
        return contextMenu;
    }

    private @NotNull MenuItem getTargetMenuItem(Target target) {
        final MenuItem startSpoofingMenuItem = new MenuItem(START_SPOOFING_TEXT);
        startSpoofingMenuItem.setOnAction(ignored -> {
            try {
                spoof(target);
                Frame statisticsFrame = Frame.createStatisticsDetails();
                statisticsFrame.setArgs(new Object[]{target});
                frameService.createNewScene(statisticsFrame, Frame.createMainFrame().getKey());
                refreshAction.run();
            } catch (NotFoundException ex) {
                ErrorHandler.handle(ex);
            }
        });
        return startSpoofingMenuItem;
    }

    private void spoof(Target target) throws NotFoundException {
        final Interface anInterface = dataService.findInterfaceByTarget(target)
                .orElseThrow(() -> new NotFoundException("Interface not found for the target"));
        final Gateway gateway = anInterface.getGateway();
        if (gateway == null) {
            throw new NotFoundException("Gateway not found for the target");
        }
        arpSpoofService.spoof(anInterface.getDeviceName(), target, gateway);
    }

    private void showDetails(Device device) {
        switch (device) {
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
            case Interface ignored -> {
                // ignored
            }
        }
    }

    private void handlePopupClose(final Stage stage, Frame frame) {
        stage.setOnCloseRequest(ignored -> frameService.stopStage(frame.getKey()));
    }

    public void rescan() {
        try {
            dataService.run();
            // Update device positions after scan
        } catch (DrawNetworkException e) {
            throw new RuntimeException(e);
        }
    }
}