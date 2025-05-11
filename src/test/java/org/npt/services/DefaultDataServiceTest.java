package org.npt.services;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npt.data.DataService;
import org.npt.data.defaults.DefaultDataService;
import org.npt.models.Gateway;
import org.npt.models.IpAddress;
import org.npt.models.SelfDevice;
import org.npt.models.Target;
import org.npt.networkservices.DeviceService;
import org.npt.networkservices.defaults.DeviceServiceImpl;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DefaultDataServiceTest {

    private static final List<Gateway> gateways = Arrays.asList(
            new Gateway("Test Device", "eth0Device", Arrays.asList("192.168.178.55"), 0, 0, null, Arrays.asList()),
            new Gateway("Test Device", "eth0Device", Arrays.asList("192.168.178.55"), 0, 0, null, Arrays.asList()),
            new Gateway("Test Device", "eth0Device", Arrays.asList("192.168.178.55"), 0, 0, null, Arrays.asList())
    );


    @BeforeAll
    public static void setup() throws SocketException, UnknownHostException {
        // Mocking DeviceService
        DeviceServiceImpl deviceService = mock(DeviceServiceImpl.class);
        when(deviceService.scanCurrentGateways())
                .thenReturn(gateways);
        when(deviceService.scanActualDevice(any()))
                .thenReturn(new SelfDevice("Test", Arrays.asList(new IpAddress("192.168.178.66", "eth0Device")), 0, 0, null, gateways));
    }

    @AfterAll
    public static void clean() {
        // Clean up if needed
    }

    @Test
    public void testScanCurrentGateways() throws SocketException, UnknownHostException {
        // Create a mock of the DeviceService class
        DeviceService deviceService = mock(DeviceService.class);

        // Prepare the expected return value
        List<Gateway> gateways = new ArrayList<>();
        // Add items to gateways as needed

        // Mock the method
        when(deviceService.scanCurrentGateways()).thenReturn(gateways);

        // Call the method
        List<Gateway> result = deviceService.scanCurrentGateways();

        // Assert that the mock returns the expected result
        Assertions.assertEquals(gateways, result);
    }

    @Test
    public void test() throws Exception {

    }
}
