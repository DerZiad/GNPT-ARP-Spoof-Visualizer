package org.npt.services;

import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.npt.exception.DrawNetworkException;
import org.npt.models.Device;
import org.npt.models.SelfDevice;

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
     * @param device The device to add.
     */
    void addDevice(@NotNull final Device device);

    /**
     * Removes a device from the list using its index.
     *
     * @param index The index of the device to remove.
     */
    void removeByIndex(@NotNull final Integer index);

    /**
     * Removes a specific device from the list.
     *
     * @param device The device instance to remove.
     */
    void removeByObject(@NotNull final Device device);

    /**
     * Retrieves a device from the list by index.
     *
     * @param index The index of the device to retrieve.
     * @return The device at the specified index.
     */
    Device getDevice(@NotNull final Integer index);

    /**
     * Runs the initial network scan and initializes internal state,
     * including self device and gateway discovery.
     *
     * @throws DrawNetworkException If the network scanning fails.
     */
    void run() throws DrawNetworkException;

    /**
     * Retrieves a map of all devices of a specific type.
     *
     * @param tClass The class type to filter devices by.
     * @param <T>    The generic type of devices.
     * @return A map of index-to-device entries for the matching type.
     */
    <T> HashMap<Integer, T> getDevices(@NotNull final Class<T> tClass);

    /**
     * Clears all internal device data.
     */
    void clear();

    /**
     * Gets the self device (i.e., the current machine running this service).
     *
     * @return The local self device.
     */
    SelfDevice getSelfDevice();

    /**
     * Retrieves the list of all known devices.
     *
     * @return A list of all devices.
     */
    List<Device> getDevices();
}
