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
 * creating targets, and finding gateways. Implementations should handle network discovery,
 * device management, and interface/gateway associations.
 *
 * <p>
 * The architecture expects DataService implementations to maintain a representation of the
 * current device (SelfDevice), its interfaces, and associated gateways and targets.
 * </p>
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
     *   <li>Device name is empty, null, or already exists for the specified gateway</li>
     *   <li>Network interface is empty, null, does not exist, or has no associated gateway</li>
     *   <li>IP address is empty, null, not a valid IPv4 address, or already exists for the gateway</li>
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
     * Searches all scanned interfaces for one that contains the specified Target.
     * Returns an empty Optional if not found.
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

    /**
     * Removes the specified device (Target or Gateway) from the managed interfaces/gateways.
     *
     * <p>
     * Implementations should ensure that the device is removed from all relevant collections,
     * including gateways and interfaces.
     * </p>
     *
     * @param device the device to remove
     */
    void remove(Device device);
}
