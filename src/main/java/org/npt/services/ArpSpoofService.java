package org.npt.services;

import org.npt.exception.NotFoundException;
import org.npt.models.Gateway;
import org.npt.models.Target;
import org.npt.services.defaults.DefaultArpSpoofService;

import java.util.Optional;

/**
 * Service interface for managing ARP spoofing processes.
 * <p>
 * Provides methods to start, stop, retrieve, and clear ARP spoofing operations
 * for specified network targets and gateways.
 */
public interface ArpSpoofService {

    /**
     * Retrieves the ARP spoofing process associated with the specified target.
     *
     * @param target the target device
     * @return an {@code Optional} containing the {@link DefaultArpSpoofService.ArpSpoofProcess} if it exists,
     * or an empty {@code Optional} if not found
     */
    Optional<DefaultArpSpoofService.ArpSpoofProcess> getArpSpoofProcess(Target target);

    /**
     * Stops the specified ARP spoofing process.
     *
     * @param arpSpoofProcess the spoofing process to stop
     */
    void stop(DefaultArpSpoofService.ArpSpoofProcess arpSpoofProcess);

    /**
     * Starts an ARP spoofing process using the given interface, target, and gateway.
     *
     * @param scanInterface the network interface to use for spoofing
     * @param target        the target device to spoof
     * @param gateway       the gateway device to impersonate
     * @throws NotFoundException if the target or gateway does not have a valid IPv4 address
     */
    void spoof(String scanInterface, Target target, Gateway gateway) throws NotFoundException;

    /**
     * Clears and stops all active ARP spoofing processes.
     */
    void clear();
}
