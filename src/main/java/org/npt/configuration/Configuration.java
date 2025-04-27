package org.npt.configuration;

import org.npt.beans.implementation.LogStorageCreator;
import org.npt.models.Connection;
import org.npt.models.Device;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    public static Device gateway = null;
    public static List<Device> devices = new ArrayList<>();
    public static Device myDevice = null;
    public static String scanInterface = null;
    public static String logStorageFolder = LogStorageCreator.createLogStorageFolder();
    public static List<Connection> connections = new ArrayList<>();

}
