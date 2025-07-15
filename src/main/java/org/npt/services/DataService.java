package org.npt.services;

import org.jetbrains.annotations.NotNull;
import org.npt.exception.DrawNetworkException;
import org.npt.exception.InvalidInputException;
import org.npt.models.Device;
import org.npt.models.Gateway;
import org.npt.models.SelfDevice;
import org.npt.models.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing and interacting with network device data.
 * <p>
 * This service provides methods for:
 * <ul>
 *     <li>Running a scan and collecting data</li>
 *     <li>Adding and removing devices</li>
 *     <li>Querying devices and device types</li>
 *     <li>Retrieving local/self device information</li>
 * </ul>
 */
public interface DataService {

    /**
     * Adds a new device to the internal list.
     *
     * @param device The {@link Device} object to be added.
     */
    void addDevice(@NotNull final Device device);

    /**
     * Removes a device from the list by its index.
     *
     * @param index The index of the {@link Device} to remove.
     */
    void removeByIndex(@NotNull final Integer index);

    /**
     * Removes a specific device object from the list.
     *
     * @param device The {@link Device} instance to remove.
     */
    void removeByObject(@NotNull final Device device);

    /**
     * Retrieves a device from the list by index.
     *
     * @param index The index of the device to retrieve.
     * @return The {@link Device} at the specified index.
     */
    Device getDevice(@NotNull final Integer index);

    /**
     * Initiates a network scan to detect and initialize internal state,
     * including the self device and connected gateways.
     *
     * @throws DrawNetworkException If an error occurs during network scanning.
     */
    void run() throws DrawNetworkException;

    /**
     * Retrieves all devices of a specific type.
     *
     * @param tClass The class type to filter devices by.
     * @param <T>    The type parameter representing the device type.
     * @return A {@link HashMap} mapping index to devices of type {@code T}.
     */
    <T> HashMap<Integer, T> getDevices(@NotNull final Class<T> tClass);

    /**
     * Clears all devices and internal data.
     */
    void clear();

    /**
     * Gets the self-representing device (usually the host running this service).
     *
     * @return The {@link SelfDevice} instance.
     */
    SelfDevice getSelfDevice();

    /**
     * Retrieves a list of all known devices.
     *
     * @return A {@link List} of all {@link Device} instances.
     */
    List<Device> getDevices();

    /**
     * Creates a {@link Target} device based on input parameters.
     *
     * @param deviceName      The name of the device.
     * @param deviceInterface The network interface used by the device.
     * @param ipAddresses     An array of IP addresses associated with the device.
     * @return A new {@link Target} instance.
     * @throws InvalidInputException If input parameters are invalid.
     */
    Target createTarget(final String deviceName, final String deviceInterface, final String[] ipAddresses) throws InvalidInputException;

    /**
     * Creates a {@link Gateway} device using provided input data.
     *
     * @param deviceName      The name of the gateway device.
     * @param deviceInterface The network interface used by the gateway.
     * @param ipAddresses     An array of IP addresses for the gateway.
     * @param nextDevices     The downstream devices (targets) connected to the gateway.
     * @return A new {@link Gateway} instance.
     * @throws InvalidInputException If input parameters are invalid.
     */
    Gateway createGateway(final String deviceName, final String deviceInterface, final String[] ipAddresses, final Target[] nextDevices) throws InvalidInputException;

    /**
     * Searches for a gateway that contains the specified target.
     *
     * @param target The {@link Target} to search for.
     * @return An {@link Optional} containing the matching {@link Gateway}, if found.
     */
    Optional<Gateway> findGatewayByTarget(final Target target);
}
