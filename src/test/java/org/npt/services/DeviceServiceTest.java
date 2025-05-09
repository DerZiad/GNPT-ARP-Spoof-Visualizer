package org.npt.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npt.models.Gateway;
import org.npt.models.IpAddress;
import org.npt.models.SelfDevice;
import org.npt.networkservices.defaults.DeviceServiceImpl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceTest {

    private DeviceServiceImpl deviceService;

    @BeforeEach
    void setUp() {
        deviceService = spy(new DeviceServiceImpl());
    }

    @Test
    void testScanCurrentGateways() throws Exception {
        // Mock IpAddress list
        List<IpAddress> mockIps = List.of(
                new IpAddress("192.168.1.10", "eth0"),
                new IpAddress("192.168.1.11", "eth0")
        );

        // Mock scanInterfaces to return mock IPs
        doReturn(mockIps).when(deviceService).scanInterfaces();

        // Mock InetAddress behavior
        try (
                MockedStatic<InetAddress> inetAddressStatic = mockStatic(InetAddress.class)
        ) {
            InetAddress inet1 = mock(InetAddress.class);
            InetAddress inet2 = mock(InetAddress.class);
            InetAddress gw1 = mock(InetAddress.class);
            InetAddress gw2 = mock(InetAddress.class);

            inetAddressStatic.when(() -> InetAddress.getByName("192.168.1.10")).thenReturn(inet1);
            inetAddressStatic.when(() -> InetAddress.getByName("192.168.1.11")).thenReturn(inet2);

            when(inet1.getAddress()).thenReturn(new byte[]{(byte) 192, (byte) 168, 1, 10});
            when(inet2.getAddress()).thenReturn(new byte[]{(byte) 192, (byte) 168, 1, 11});

            inetAddressStatic.when(() -> InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, 1}))
                    .thenReturn(gw1)
                    .thenReturn(gw2);

            when(gw1.getHostAddress()).thenReturn("192.168.1.1");
            when(gw2.getHostAddress()).thenReturn("192.168.1.1");

            List<Gateway> result = deviceService.scanCurrentGateways();

            assertEquals(1, result.size());
            Gateway gateway = result.getFirst();
            assertEquals("eth0", gateway.getNetworkInterface());
            assertTrue(gateway.getIpAddresses().contains("192.168.1.1"));
        }
    }

    @Test
    void testScanActualDevice() throws Exception {
        List<IpAddress> mockIps = List.of(new IpAddress("192.168.1.10", "eth0"));
        doReturn(mockIps).when(deviceService).scanInterfaces();

        List<Gateway> gateways = List.of(new Gateway("Router", "eth0", new ArrayList<>(), 0, 0, null, new ArrayList<>()));
        SelfDevice device = deviceService.scanActualDevice(gateways);

        assertEquals("My Device", device.getDeviceName());
        assertEquals(mockIps, device.getIpAddresses());
        assertEquals(gateways, device.getNextGateways());
    }
}


