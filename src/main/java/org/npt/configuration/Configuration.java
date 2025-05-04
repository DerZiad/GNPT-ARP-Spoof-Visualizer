package org.npt.configuration;

import org.npt.beans.implementation.LogStorageCreator;
import org.npt.models.Gateway;
import org.npt.models.SelfDevice;
import org.npt.models.Target;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    public static List<Target> targets = new ArrayList<>();
    public static SelfDevice selfDevice = null;
    public static List<Gateway> gateways = new ArrayList<>();
    public static String logStorageFolder = LogStorageCreator.createLogStorageFolder();

}
