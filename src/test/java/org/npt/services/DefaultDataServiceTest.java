package org.npt.services;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import org.junit.jupiter.api.*;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

public class DefaultDataServiceTest {

    private static final List<Gateway> gateways = Arrays.asList(
            new Gateway("Test Device", "eth0Device", Arrays.asList("192.168.178.55"), 0, 0, null, Arrays.asList()),
            new Gateway("Test Device", "eth0Device", Arrays.asList("192.168.178.55"), 0, 0, null, Arrays.asList()),
            new Gateway("Test Device", "eth0Device", Arrays.asList("192.168.178.55"), 0, 0, null, Arrays.asList())
    );

    @BeforeAll
    public static void setup() throws SocketException, UnknownHostException, InterruptedException {
        // Mocking DeviceService
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
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
    public void test() {
        // given
        DataService dataService = DefaultDataService.getInstance();

        // when, then
        assertThat(dataService.getDevices().size()).isEqualTo(gateways.size());
    }
}
