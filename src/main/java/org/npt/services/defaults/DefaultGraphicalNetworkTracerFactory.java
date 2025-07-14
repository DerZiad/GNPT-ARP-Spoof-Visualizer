package org.npt.services.defaults;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.npt.exception.ShutdownException;
import org.npt.models.KnownHost;
import org.npt.services.ArpSpoofService;
import org.npt.services.DataService;
import org.npt.services.GatewayService;
import org.npt.services.GraphicalNetworkTracerFactory;
import org.npt.services.TargetService;

import lombok.Getter;

public class DefaultGraphicalNetworkTracerFactory implements GraphicalNetworkTracerFactory {

    private static DefaultGraphicalNetworkTracerFactory instance = null;
    private static final String PATH = "/org/npt/%s";

    private DefaultGraphicalNetworkTracerFactory() {

        try {
            run();
        } catch (ShutdownException e) {
            // TODO shutdownException should be handled properly
            throw new RuntimeException(e);
        }
    }

    @Getter
    private final HashMap<String, KnownHost> knownHosts = new HashMap<>();

    @Override
    public ArpSpoofService getArpSpoofService() {
        return DefaultArpSpoofService.getInstance();
    }

    @Override
    public DataService getDataService() {
        return DefaultDataService.getInstance();
    }

    @Override
    public TargetService getTargetService() {
        return DefaultTargetService.getInstance();
    }

    @Override
    public GatewayService getGatewayService() {
        return DefaultGatewayService.getInstance();
    }

    @Override
    public InputStream getResource(String name) {
        String resourcePath = String.format(PATH, name);
        return this.getClass().getResourceAsStream(resourcePath);
    }

    public static DefaultGraphicalNetworkTracerFactory getInstance() {
        if (instance == null) {
            instance = new DefaultGraphicalNetworkTracerFactory();
        }
        return instance;
    }

    // Private method to initiate KnownHosts if needed
    private void run() throws ShutdownException {

        InputStream is = getResource("settings/npt.properties");
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
            throw new ShutdownException(String.format(ShutdownException.ERROR_FORMAT, this.getClass().getName(), "Failed to load property File"), ShutdownException.ShutdownExceptionErrorCode.FAILED_TO_LOAD_PROPERTY_FILE);
        }

        Set<String> keys = props.stringPropertyNames();
        for (String key : keys) {
            if (key.endsWith(".icon")) {
                String appName = key.substring(0, key.indexOf(".icon"));
                String iconPath = props.getProperty(key);
                String ipKey = appName + ".ips";
                String ipValue = props.getProperty(ipKey, "");
                List<String> ipList = Arrays.asList(ipValue.split("\\s*,\\s*"));
                knownHosts.put(appName, new KnownHost(appName, iconPath, ipList));
            }
        }

    }
}
