package org.npt.configuration;

import org.npt.models.Device;
import org.npt.models.Gateway;
import org.npt.models.SelfDevice;
import org.npt.models.Target;
import org.npt.services.impl.LogStorageCreator;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    public static String logStorageFolder = LogStorageCreator.createLogStorageFolder();

}
