package org.npt.services.services;

import org.junit.jupiter.api.*;
import org.npt.exception.InvalidInputException;
import org.npt.models.Gateway;
import org.npt.models.Interface;
import org.npt.models.Target;
import org.npt.services.DataService;
import org.npt.services.GraphicalNetworkTracerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@DisplayName("DataService Behavior Tests")
public class DataServiceUnitTest {

    private static final String TEST_IP = "192.168.0.100";
    private static final String TEST_DEVICE_NAME = "TestDevice";
    private static final String TEST_DEVICE_INTERFACE = "eth-test0";

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
    @DisplayName("Should add a Target device")
    public void testAddDeviceForTarget() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_IP);
        dataService.addDevice(target);

        assertThat(dataService.getDevices()).contains(target);
    }

    @Test
    @DisplayName("Should remove device by object")
    public void testRemoveByObject() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_IP);
        dataService.addDevice(target);
        dataService.removeByObject(target);

        assertThat(dataService.getDevices()).doesNotContain(target);
    }

    @Test
    @DisplayName("Should remove device by index")
    public void testRemoveByIndex() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_IP);
        dataService.addDevice(target);
        dataService.removeByIndex(0);

        assertThat(dataService.getDevices()).doesNotContain(target);
    }

    @Test
    @DisplayName("Should get device by index")
    public void testGetDevice() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_IP);
        dataService.addDevice(target);

        assertThat(dataService.getDevice(0)).isEqualTo(target);
    }

    @Test
    @DisplayName("Should filter devices by type")
    public void testGetDevicesByClass() {
        Target t1 = new Target(TEST_DEVICE_NAME, TEST_IP);
        Target t2 = new Target(TEST_DEVICE_NAME + "2", TEST_IP);
        Gateway g1 = new Gateway("Router", "192.168.0.1", List.of(t1));

        dataService.addDevice(t1);
        dataService.addDevice(t2);
        dataService.addDevice(g1);

        HashMap<Integer, Target> targets = dataService.getDevices(Target.class);
        HashMap<Integer, Gateway> gateways = dataService.getDevices(Gateway.class);

        assertThat(targets).hasSize(2);
        assertThat(gateways).hasSize(1);
    }

    @Test
    @DisplayName("Should clear all devices")
    public void testClear() {
        dataService.addDevice(new Target(TEST_DEVICE_NAME, TEST_IP));
        dataService.clear();

        assertThat(dataService.getDevices()).isEmpty();
    }

    @Test
    @DisplayName("Should create target with valid input")
    public void testCreateTargetValid() throws InvalidInputException {
        Optional<Interface> networkInterface = dataService.getSelfDevice().getAnInterfaces().stream().findFirst();
        Assumptions.assumeTrue(networkInterface.isPresent());

        String iface = networkInterface.get().getDeviceName();
        Target target = dataService.createTarget(TEST_DEVICE_NAME, iface, TEST_IP);

        assertThat(target).isNotNull();
        assertThat(dataService.getDevices()).contains(target);
    }

    @Test
    @DisplayName("Should throw exception when creating invalid target (missing IP)")
    public void testCreateTargetInvalidMissingIp() {
        InvalidInputException thrown = catchThrowableOfType(() ->
                dataService.createTarget(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, null), InvalidInputException.class);

        assertThat(thrown).isNotNull();
        assertThat(thrown.getErrors()).containsKey("IP Address");
    }

    @Test
    @DisplayName("Should throw exception when creating invalid target (duplicate name)")
    public void testCreateTargetInvalidDuplicateName() throws InvalidInputException {
        Optional<Interface> networkInterface = dataService.getSelfDevice().getAnInterfaces().stream().findFirst();
        Assumptions.assumeTrue(networkInterface.isPresent());

        String iface = networkInterface.get().getDeviceName();
        dataService.createTarget(TEST_DEVICE_NAME, iface, "192.168.0.101");

        InvalidInputException thrown = catchThrowableOfType(() ->
                dataService.createTarget(TEST_DEVICE_NAME, iface, "192.168.0.102"), InvalidInputException.class);

        assertThat(thrown).isNotNull();
        assertThat(thrown.getErrors()).containsKey("Device Name");
    }

    @Test
    @DisplayName("Should throw exception when creating invalid target (blank name)")
    public void testCreateTargetInvalidBlankName() {
        InvalidInputException thrown = catchThrowableOfType(() ->
                dataService.createTarget("", TEST_DEVICE_INTERFACE, TEST_IP), InvalidInputException.class);

        assertThat(thrown).isNotNull();
        assertThat(thrown.getErrors()).containsKey("Device Name");
    }

    @Test
    @DisplayName("Should return empty Optional from findGatewayByTarget")
    public void testFindGatewayByTarget() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_IP);
        Optional<Gateway> result = dataService.findGatewayByTarget(target);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return same singleton instance")
    public void testSingletonInstance() {
        DataService instance1 = dataService;
        DataService instance2 = GraphicalNetworkTracerFactory.getInstance().getDataService();

        assertThat(instance1).isSameAs(instance2);
    }
}
