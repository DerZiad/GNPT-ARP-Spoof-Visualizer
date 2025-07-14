package org.npt.services.services;

import org.junit.jupiter.api.*;
import org.npt.models.*;
import org.npt.services.DataService;
import org.npt.services.GraphicalNetworkTracerFactory;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DataService Behavior Tests")
public class DataServiceUnitTest {

    private static final String TEST_IP = "127.0.0.1";
    private static final String TEST_DEVICE_NAME = "Test";
    private static final String TEST_DEVICE_INTERFACE = "testeth0";

    private static DataService dataService;

    @BeforeAll
    public static void setup() throws Exception {
        dataService = GraphicalNetworkTracerFactory.getInstance().getDataService();
        dataService.run();
    }

    @AfterEach
    public void clean() {
        dataService.clear();
    }

    @Test
    @DisplayName("Should add a device and reflect in device list")
    public void testAddDeviceForTarget() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP));
        dataService.addDevice(target);

        assertThat(dataService.getDevices()).contains(target);
    }

    @Test
    @DisplayName("Should remove device by object")
    public void testRemoveByObject() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP));
        dataService.addDevice(target);
        dataService.removeByObject(target);

        assertThat(dataService.getDevices()).doesNotContain(target);
    }

    @Test
    @DisplayName("Should remove device by index")
    public void testRemoveByIndex() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP));
        dataService.addDevice(target);
        dataService.removeByIndex(0);

        assertThat(dataService.getDevices()).doesNotContain(target);
    }

    @Test
    @DisplayName("Should get device by index or return null if not found")
    public void testGetDevice() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP));
        dataService.addDevice(target);

        assertThat(dataService.getDevice(0)).isEqualTo(target);
    }

    @Test
    @DisplayName("Should filter devices by class type")
    public void testGetDevicesByClass() {
        Target t1 = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP));
        Target t2 = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP));
        Target t3 = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP));
        Gateway g1 = new Gateway(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), List.of(t1, t2));
        Gateway g2 = new Gateway(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), List.of(t3));

        dataService.addDevice(t1);
        dataService.addDevice(t2);
        dataService.addDevice(t3);
        dataService.addDevice(g1);
        dataService.addDevice(g2);

        HashMap<Integer, Target> targets = dataService.getDevices(Target.class);
        HashMap<Integer, Gateway> gateways = dataService.getDevices(Gateway.class);

        assertThat(targets).hasSize(3);
        assertThat(gateways).hasSize(2);
    }
}
