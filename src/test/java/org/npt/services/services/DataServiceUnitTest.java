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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataServiceUnitTest {

    private static final String TEST_IP = "192.168.0.100";
    private static final String TEST_DEVICE_NAME = "TestDevice";
    private static final String TEST_DEVICE_INTERFACE = "eth-test0";

    private DataService dataService;

    @BeforeAll
    void setup() throws Exception {
        dataService = GraphicalNetworkTracerFactory.getInstance().getDataService();
        dataService.run(); // initializes interfaces and gateways
    }

    @AfterEach
    void clean() {
        dataService.clear();
    }

    @Test
    void shouldAddDevice() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_IP);
        dataService.addDevice(target);

        assertThat(dataService.getDevices()).contains(target);
    }

    @Test
    void shouldRemoveDeviceByObject() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_IP);
        dataService.addDevice(target);
        dataService.removeByObject(target);

        assertThat(dataService.getDevices()).doesNotContain(target);
    }

    @Test
    void shouldRemoveDeviceByIndex() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_IP);
        dataService.addDevice(target);
        dataService.removeByIndex(0);

        assertThat(dataService.getDevices()).doesNotContain(target);
    }

    @Test
    void shouldReturnDeviceByIndex() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_IP);
        dataService.addDevice(target);

        assertThat(dataService.getDevice(0)).isEqualTo(target);
    }

    @Test
    void shouldFilterDevicesByClass() {
        Target t1 = new Target("T1", "192.168.1.2");
        Target t2 = new Target("T2", "192.168.1.3");
        Gateway g = new Gateway("G1", "192.168.1.1", List.of(t1, t2));

        dataService.addDevice(t1);
        dataService.addDevice(t2);
        dataService.addDevice(g);

        HashMap<Integer, Target> targets = dataService.getDevices(Target.class);
        HashMap<Integer, Gateway> gateways = dataService.getDevices(Gateway.class);

        assertThat(targets.values()).contains(t1, t2);
        assertThat(gateways.values()).contains(g);
    }

    @Test
    void shouldClearDevices() {
        dataService.addDevice(new Target(TEST_DEVICE_NAME, TEST_IP));
        dataService.clear();

        assertThat(dataService.getDevices()).isEmpty();
    }

    @Test
    void shouldCreateTargetWithValidInput() throws InvalidInputException {
        Optional<Interface> opt = dataService.getSelfDevice().getAnInterfaces().stream().filter(i -> i.getGatewayOptional().isPresent()).findFirst();
        Assumptions.assumeTrue(opt.isPresent());

        Target target = dataService.createTarget(TEST_DEVICE_NAME, opt.get().getDeviceName(), TEST_IP);

        assertThat(target).isNotNull();
        assertThat(dataService.getDevices()).contains(target);
    }

    @Test
    void shouldFailCreateTargetWhenInterfaceMissing() {
        InvalidInputException ex = catchThrowableOfType(() ->
                dataService.createTarget(TEST_DEVICE_NAME, "non-existent", TEST_IP), InvalidInputException.class);

        assertThat(ex.getErrors()).containsKey("Network Interface");
    }

    @Test
    void shouldFailCreateTargetWhenGatewayIsMissing() {
        Optional<Interface> opt = dataService.getSelfDevice().getAnInterfaces().stream()
                .filter(i -> i.getGatewayOptional().isEmpty()).findFirst();
        Assumptions.assumeTrue(opt.isPresent());

        InvalidInputException ex = catchThrowableOfType(() ->
                dataService.createTarget(TEST_DEVICE_NAME, opt.get().getDeviceName(), TEST_IP), InvalidInputException.class);

        assertThat(ex.getErrors()).containsKey("Network Interface");
    }

    @Test
    void shouldFailCreateTargetWhenNameIsDuplicate() throws InvalidInputException {
        Optional<Interface> opt = dataService.getSelfDevice().getAnInterfaces().stream()
                .filter(i -> i.getGatewayOptional().isPresent()).findFirst();
        Assumptions.assumeTrue(opt.isPresent());

        dataService.createTarget(TEST_DEVICE_NAME, opt.get().getDeviceName(), "192.168.0.101");

        InvalidInputException ex = catchThrowableOfType(() ->
                        dataService.createTarget(TEST_DEVICE_NAME, opt.get().getDeviceName(), "192.168.0.102"),
                InvalidInputException.class);

        assertThat(ex.getErrors()).containsKey("Device Name");
    }

    @Test
    void shouldFailCreateTargetWhenIpIsNull() {
        InvalidInputException ex = catchThrowableOfType(() ->
                dataService.createTarget(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, null), InvalidInputException.class);

        assertThat(ex.getErrors()).containsKey("IP Address");
    }

    @Test
    void shouldFailCreateTargetWhenIpIsInvalid() {
        InvalidInputException ex = catchThrowableOfType(() ->
                        dataService.createTarget(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, "999.999.999.999"),
                InvalidInputException.class);

        assertThat(ex.getErrors()).containsKey("IP Address");
    }

    @Test
    void shouldFailCreateTargetWhenNameIsBlank() {
        InvalidInputException ex = catchThrowableOfType(() ->
                dataService.createTarget(" ", TEST_DEVICE_INTERFACE, TEST_IP), InvalidInputException.class);

        assertThat(ex.getErrors()).containsKey("Device Name");
    }

    @Test
    void shouldReturnEmptyWhenTargetHasNoGateway() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_IP);
        Optional<Gateway> found = dataService.findGatewayByTarget(target);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnSameSingletonInstance() {
        DataService ds1 = GraphicalNetworkTracerFactory.getInstance().getDataService();
        DataService ds2 = GraphicalNetworkTracerFactory.getInstance().getDataService();

        assertThat(ds1).isSameAs(ds2);
    }

    @Test
    void shouldAllowMultipleDeviceTypesInSameList() {
        Gateway gateway = new Gateway("G", "192.168.0.1", List.of());
        Target target = new Target("T", "192.168.0.5");

        dataService.addDevice(gateway);
        dataService.addDevice(target);

        assertThat(dataService.getDevices()).contains(gateway, target);
    }

    @Test
    void shouldHandleEmptyInterfacesOnStartupGracefully() throws Exception {
        List<Interface> interfaces = dataService.getSelfDevice().getAnInterfaces();
        assertThat(interfaces).isNotNull();
    }
}
