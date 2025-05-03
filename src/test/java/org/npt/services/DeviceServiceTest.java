package org.npt.services;

import org.junit.jupiter.api.Test;
import org.npt.models.IpAddress;
import org.npt.beans.implementation.DeviceService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.*;
import java.util.*;

public class DeviceServiceTest {

    @Test
    public void testScanInterfaces() throws Exception {
        // Mock InetAddress
        InetAddress mockAddress1 = mock(InetAddress.class);
        when(mockAddress1.getHostAddress()).thenReturn("192.168.1.10");

        // Mock NetworkInterface
        NetworkInterface mockInterface = mock(NetworkInterface.class);
        when(mockInterface.getDisplayName()).thenReturn("eth0");
        when(mockInterface.getInetAddresses())
                .thenReturn(Collections.enumeration(List.of(mockAddress1)));

        // Test the scan using the injected interfaces
        DeviceService scanner = new DeviceService();
        List<IpAddress> result = scanner.scanInterfaces();

        // Assertions
        assertEquals(1, result.size());
        assertEquals("192.168.1.10", result.getFirst().getIp());
        assertEquals("eth0", result.getFirst().getNetworkInterface());
    }
}
