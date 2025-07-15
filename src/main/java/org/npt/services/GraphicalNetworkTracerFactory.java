package org.npt.services;

import org.npt.models.KnownHost;
import org.npt.services.defaults.DefaultGraphicalNetworkTracerFactory;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Factory interface for accessing core services and resources used in graphical network tracing.
 * <p>
 * This interface provides methods to retrieve service instances and known network resources,
 * acting as a centralized access point for dependency injection and service management.
 */
public interface GraphicalNetworkTracerFactory {

    /**
     * Retrieves the {@link ArpSpoofService} instance used for ARP spoofing operations.
     *
     * @return The {@link ArpSpoofService} implementation.
     */
    ArpSpoofService getArpSpoofService();

    /**
     * Retrieves the {@link DataService} instance responsible for network device management.
     *
     * @return The {@link DataService} implementation.
     */
    DataService getDataService();

    /**
     * Returns a map of known hosts in the network.
     *
     * @return A {@link HashMap} where the key is the host identifier (e.g., IP or MAC address),
     *         and the value is a {@link KnownHost} instance.
     */
    HashMap<String, KnownHost> getKnownHosts();

    /**
     * Retrieves a resource from the classpath or bundle using the given name.
     *
     * @param name The name or path of the resource.
     * @return An {@link InputStream} for reading the resource content.
     */
    InputStream getResource(String name);

    /**
     * Provides a singleton instance of the default {@link GraphicalNetworkTracerFactory}.
     * <p>
     * This is a static method that returns the default implementation provided by
     * {@link DefaultGraphicalNetworkTracerFactory}.
     *
     * @return A singleton {@link GraphicalNetworkTracerFactory} instance.
     */
    static GraphicalNetworkTracerFactory getInstance() {
        return DefaultGraphicalNetworkTracerFactory.getInstance();
    }
}
