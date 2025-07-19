package org.npt.services;

import org.npt.exception.DrawNetworkException;
import org.npt.exception.InvalidInputException;
import org.npt.models.Device;
import org.npt.models.Interface;
import org.npt.models.SelfDevice;
import org.npt.models.Target;

import java.util.Optional;

/**
 * DataService provides methods for managing network data, including scanning the network,
 * creating targets, and finding gateways. Implementations should handle network discovery
 * and device management.
 */
public interface DataService {

    /**
     * Initializes and scans the network interfaces, discovering devices and gateways.
     *
     * @throws DrawNetworkException if network initialization or scanning fails.
     */
    void run() throws DrawNetworkException;

    /**
     * Creates a new Target device and associates it with the specified network interface and gateway.
     *
     * <p>
     * This method throws {@link InvalidInputException} if:
     * <ul>
     *   <li>Device name is empty or null</li>
     *   <li>Network interface is empty, null, does not exist, or has no associated gateway</li>
     *   <li>IP address is empty, null, or not a valid IPv4 address</li>
     *   <li>A Target with the same IP already exists for the gateway</li>
     * </ul>
     * </p>
     *
     * @param deviceName       the name of the device to create
     * @param networkInterface the network interface to associate with the target
     * @param ip               the IPv4 address of the target device
     * @return the created Target
     * @throws InvalidInputException if input validation fails (see above)
     */
    Target createTarget(String deviceName, String networkInterface, String ip) throws InvalidInputException;

    /**
     * Finds the network Interface associated with the given Target device.
     *
     * <p>
     * This method searches all scanned interfaces for one that contains the specified Target.
     * It never throws {@link InvalidInputException}; if the Target is not found, an empty Optional is returned.
     * </p>
     *
     * @param target the Target device to search for
     * @return an Optional containing the Interface if found, or empty if not found
     */
    Optional<Interface> findInterfaceByTarget(Target target);

    /**
     * Returns the SelfDevice representing the current device and its network interfaces.
     *
     * @return the SelfDevice instance
     */
    SelfDevice getSelfDevice();

    void remove(Device device);
}
