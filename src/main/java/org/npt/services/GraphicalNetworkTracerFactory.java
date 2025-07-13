package org.npt.services;

import org.npt.models.KnownHost;
import org.npt.services.defaults.DefaultGraphicalNetworkTracerFactory;

import java.util.HashMap;

public interface GraphicalNetworkTracerFactory {

    ArpSpoofService getArpSpoofService();

    DataService getDataService();

    HashMap<String, KnownHost> getKnownHosts();

    TargetService getTargetService();

    GatewayService getGatewayService();

    static GraphicalNetworkTracerFactory getInstance() {
        return DefaultGraphicalNetworkTracerFactory.getInstance();
    }
}
