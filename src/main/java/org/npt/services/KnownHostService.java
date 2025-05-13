package org.npt.services;

import lombok.Getter;
import org.npt.exception.ShutdownException;
import org.npt.models.KnownHost;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public interface KnownHostService {

    public HashMap<String, KnownHost> getKnownHosts();

    public static KnownHostService getInstance() throws ShutdownException {
        return DefaultKnownHostService.getInstance();
    }
}

class DefaultKnownHostService implements KnownHostService {

    private static DefaultKnownHostService defaultKnownHostService = null;

    @Getter
    private final HashMap<String, KnownHost> knownHosts = new HashMap<>();

    private DefaultKnownHostService() throws ShutdownException {
        run();
    }

    private void run() throws ShutdownException {
        ResourceLoader resourceLoader = ResourceLoader.getInstance();
        InputStream is = resourceLoader.getResource("settings/npt.properties");
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

    public static KnownHostService getInstance() throws ShutdownException {
        if (defaultKnownHostService == null)
            defaultKnownHostService = new DefaultKnownHostService();
        return defaultKnownHostService;
    }
}
