package org.npt.services;

import org.jetbrains.annotations.NotNull;
import org.npt.exception.DrawNetworkException;
import org.npt.exception.InvalidInputException;
import org.npt.models.*;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * Defines operations for managing network devices and interactions within a network environment.
 * <p>
 * Provides functionality for:
 * <ul>
 *     <li>Scanning the network and initializing internal state</li>
 *     <li>Adding, removing, and querying devices</li>
 *     <li>Retrieving devices by type</li>
 *     <li>Accessing the local (self) device information</li>
 * </ul>
 */
public interface DataService {

    /**
     * Adds a device to the internal collection.
     *
     * @param device the {@link Device} to be added
     */
    void addDevice(@NotNull final Device device);

    /**
     * Removes a device from the collection by its index.
     *
     * @param index the index of the {@link Device} to remove
     */
    void removeByIndex(@NotNull final Integer index);

    /**
     * Removes the specified device instance from the collection.
     *
     * @param device the {@link Device} to remove
     */
    void removeByObject(@NotNull final Device device);

    /**
     * Retrieves a device by its index.
     *
     * @param index the index of the device
     * @return the {@link Device} at the given index
     */
    Device getDevice(@NotNull final Integer index);

    /**
     * Performs a network scan and initializes internal state,
     * including available interfaces, self device, and discovered gateways.
     *
     * @throws DrawNetworkException if the scan fails due to a network error
     */
    void run() throws DrawNetworkException;

    /**
     * Retrieves all devices that are instances of the specified type.
     *
     * @param tClass the class type to filter by
     * @param <T>    the specific device subtype
     * @return a {@link HashMap} mapping indices to matching device instances
     */
    <T> HashMap<Integer, T> getDevices(@NotNull final Class<T> tClass);

    /**
     * Clears all tracked devices and resets internal state.
     */
    void clear();

    /**
     * Returns the self (local) device representing the current host.
     *
     * @return the {@link SelfDevice} instance
     */
    SelfDevice getSelfDevice();

    /**
     * Returns a list of all registered devices.
     *
     * @return a {@link List} of {@link Device} objects
     */
    List<Device> getDevices();

    /**
     * Creates a new {@link Target} device from the provided input.
     *
     * @param deviceName       the name to assign to the target device
     * @param networkInterface the name of the interface the target is associated with
     * @param ip               the IP address of the target
     * @return a new {@link Target} instance
     * @throws InvalidInputException if input validation fails
     */
    Target createTarget(String deviceName, String networkInterface, String ip) throws InvalidInputException;

    /**
     * Searches for a {@link Gateway} that contains the specified target device.
     *
     * @param target the {@link Target} to locate
     * @return an {@link Optional} containing the gateway if found, otherwise empty
     */
    Optional<Gateway> findGatewayByTarget(final Target target);

    /**
     * Rescans the network to update device states and configurations.
     * @return a {@link Queue} of {@link ChangeAfterRescan} representing changes detected after the rescan
     * @throws DrawNetworkException if the rescan fails due to a network error
     */
    Queue<ChangeAfterRescan> rescan() throws DrawNetworkException;
}
