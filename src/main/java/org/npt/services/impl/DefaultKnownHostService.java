package org.npt.services.impl;

import lombok.Getter;
import org.npt.models.KnownHost;
import org.npt.services.KnownHostService;
import org.npt.services.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DefaultKnownHostService implements KnownHostService {

    private static DefaultKnownHostService defaultKnownHostService = null;

    @Getter
    private final HashMap<String, KnownHost> knownHosts = new HashMap<>();

    private DefaultKnownHostService(){
        run();
    }

    private void run() {
        ResourceLoader resourceLoader = ResourceLoaderImpl.getInstance();
        try {
            InputStream is = resourceLoader.getResource("settings/npt.properties");
            Properties props = new Properties();
            props.load(is);

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static KnownHostService getInstance(){
        if(defaultKnownHostService == null)
            defaultKnownHostService = new DefaultKnownHostService();
        return defaultKnownHostService;
    }
}
