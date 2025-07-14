package org.npt.services;

import org.npt.models.KnownHost;
import org.npt.services.defaults.DefaultGraphicalNetworkTracerFactory;

import java.io.InputStream;
import java.util.HashMap;

public interface GraphicalNetworkTracerFactory {

    ArpSpoofService getArpSpoofService();

    DataService getDataService();

    HashMap<String, KnownHost> getKnownHosts();

    TargetService getTargetService();

    GatewayService getGatewayService();

    InputStream getResource(String name);

    static GraphicalNetworkTracerFactory getInstance() {
        return DefaultGraphicalNetworkTracerFactory.getInstance();
    }
}
