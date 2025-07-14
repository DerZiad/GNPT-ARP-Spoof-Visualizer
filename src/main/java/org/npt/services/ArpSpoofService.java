package org.npt.services;

import java.util.Optional;

import org.npt.exception.NotFoundException;
import org.npt.models.Gateway;
import org.npt.models.Target;
import org.npt.services.defaults.DefaultArpSpoofService;

/**
 * Service interface for managing ARP spoofing processes.
 * This service allows starting and stopping ARP spoofing for target devices
 * and provides methods to retrieve the current ARP spoofing process.
 */
public interface ArpSpoofService {

    /**
     * Retrieves the ARP spoofing process for the given target.
     *
     * @param target The target device.
     * @return An Optional containing the ArpSpoofProcess if found, otherwise empty.
     */
    Optional<DefaultArpSpoofService.ArpSpoofProcess> getArpSpoofProcess(Target target);

    /**
     * Stops the ARP spoofing for the given target and gateway.
     *
     * @param target  The target device.
     * @param gateway The gateway device.
     * @throws NotFoundException if the target or gateway does not have a valid IPv4 address.
     */
    void stop(Target target, Gateway gateway) throws NotFoundException;

    /**
     * Starts ARP spoofing for the given scan interface, target, and gateway.
     *
     * @param scanInterface The network interface to use for scanning.
     * @param target        The target device.
     * @param gateway       The gateway device.
     * @throws NotFoundException if the target or gateway does not have a valid IPv4 address.
     */
    void spoof(final String scanInterface, final Target target, final Gateway gateway) throws NotFoundException;

    /**
     * Clears all ARP spoofing processes and stops any ongoing tasks.
     */
    void clear();
}
