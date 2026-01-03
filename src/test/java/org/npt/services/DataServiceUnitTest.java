package org.npt.services;

import org.junit.jupiter.api.*;
import org.npt.exception.InvalidInputException;
import org.npt.models.Interface;
import org.npt.models.Target;
import org.npt.services.DataService;
import org.npt.services.GraphicalNetworkTracerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@DisplayName("DataService Unit Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataServiceUnitTest {

    private static final String TEST_IP_PREFIX = "192.168.0.";
    private DataService dataService;
    private Interface testInterface;
    private String validInterfaceName;
    private final String nonExistentInterfaceName = "non-existent-" + UUID.randomUUID();

    @BeforeAll
    void setup() throws Exception {
        dataService = GraphicalNetworkTracerFactory.getInstance().getDataService();
        dataService.run();

        Optional<Interface> interfaceWithGateway = dataService.getSelfDevice().getAnInterfaces().stream()
                .filter(i -> i.getGateway() != null)
                .findFirst();

        Assumptions.assumeTrue(interfaceWithGateway.isPresent(), "At least one interface.png with gateway required");
        testInterface = interfaceWithGateway.get();
        validInterfaceName = testInterface.getDeviceName();
    }

    @BeforeEach
    void cleanupTargets() {
        dataService.getSelfDevice().getAnInterfaces().stream()
                .filter(i -> i.getGateway() != null)
                .forEach(i -> {
                    List<Target> targetsToRemove = i.getGateway().getDevices().stream()
                            .filter(t -> t.getDeviceName().startsWith("Test"))
                            .toList();
                    targetsToRemove.forEach(dataService::remove);
                });
    }

    @Test
    void createTarget_validInput_success() throws InvalidInputException {
        String ip = TEST_IP_PREFIX + "101";
        String name = "TestTarget-" + UUID.randomUUID().toString().substring(0, 8);
        Target target = dataService.createTarget(name, validInterfaceName, ip);

        assertThat(target).isNotNull();
        assertThat(target.getDeviceName()).isEqualTo(name);
        assertThat(target.getIp()).isEqualTo(ip);
        assertThat(testInterface.getGateway().getDevices()).contains(target);
    }

    @Test
    void createTarget_interfaceDoesNotExist_fails() {
        InvalidInputException ex = catchThrowableOfType(() ->
                dataService.createTarget("TestTarget", nonExistentInterfaceName, TEST_IP_PREFIX + "102"),
                InvalidInputException.class);

        assertThat(ex).isNotNull();
        assertThat(ex.getErrors()).containsKey("Network Interface");
    }

    @Test
    void createTarget_interfaceHasNoGateway_fails() {
        Optional<Interface> noGateway = dataService.getSelfDevice().getAnInterfaces().stream()
                .filter(i -> i.getGateway() == null)
                .findFirst();

        Assumptions.assumeTrue(noGateway.isPresent(), "At least one interface.png without gateway required");

        InvalidInputException ex = catchThrowableOfType(() ->
                dataService.createTarget("TestTarget", noGateway.get().getDeviceName(), TEST_IP_PREFIX + "103"),
                InvalidInputException.class);

        assertThat(ex).isNotNull();
        assertThat(ex.getErrors()).containsKey("Network Interface");
    }

    @Test
    void createTarget_blankOrNullDeviceName_fails() {
        InvalidInputException ex1 = catchThrowableOfType(() ->
                dataService.createTarget("", validInterfaceName, TEST_IP_PREFIX + "104"),
                InvalidInputException.class);
        assertThat(ex1.getErrors()).containsKey("Device Name");

        InvalidInputException ex2 = catchThrowableOfType(() ->
                dataService.createTarget(null, validInterfaceName, TEST_IP_PREFIX + "105"),
                InvalidInputException.class);
        assertThat(ex2.getErrors()).containsKey("Device Name");
    }

    @Test
    void createTarget_blankOrNullIp_fails() {
        InvalidInputException ex1 = catchThrowableOfType(() ->
                dataService.createTarget("TestTarget", validInterfaceName, ""),
                InvalidInputException.class);
        assertThat(ex1.getErrors()).containsKey("IP Address");

        InvalidInputException ex2 = catchThrowableOfType(() ->
                dataService.createTarget("TestTarget", validInterfaceName, null),
                InvalidInputException.class);
        assertThat(ex2.getErrors()).containsKey("IP Address");
    }

    @Test
    void createTarget_invalidIpFormat_fails() {
        InvalidInputException ex = catchThrowableOfType(() ->
                dataService.createTarget("TestTarget", validInterfaceName, "999.999.999.999"),
                InvalidInputException.class);
        assertThat(ex.getErrors()).containsKey("IP Address");
    }

    @Test
    void createTarget_duplicateIpForGateway_fails() throws InvalidInputException {
        String ip = TEST_IP_PREFIX + "106";
        Target t1 = dataService.createTarget("TestTarget1", validInterfaceName, ip);

        InvalidInputException ex = catchThrowableOfType(() ->
                dataService.createTarget("TestTarget2", validInterfaceName, ip),
                InvalidInputException.class);

        assertThat(ex).isNotNull();
        assertThat(ex.getErrors()).containsKey("IP Address");
        dataService.remove(t1);
    }

    @Test
    void createTarget_sameIpDifferentGateways_success() throws InvalidInputException {
        List<Interface> interfaces = dataService.getSelfDevice().getAnInterfaces().stream()
                .filter(i -> i.getGateway() != null)
                .limit(2)
                .toList();

        Assumptions.assumeTrue(interfaces.size() == 2, "At least two interfaces with gateway required");

        String ip = TEST_IP_PREFIX + "107";
        Target t1 = dataService.createTarget("TestTargetA", interfaces.get(0).getDeviceName(), ip);
        Target t2 = dataService.createTarget("TestTargetB", interfaces.get(1).getDeviceName(), ip);

        assertThat(interfaces.get(0).getGateway().getDevices()).contains(t1);
        assertThat(interfaces.get(1).getGateway().getDevices()).contains(t2);

        dataService.remove(t1);
        dataService.remove(t2);
    }

    @Test
    void singletonInstance_isSame() {
        DataService ds1 = GraphicalNetworkTracerFactory.getInstance().getDataService();
        DataService ds2 = GraphicalNetworkTracerFactory.getInstance().getDataService();
        assertThat(ds1).isSameAs(ds2);
    }

    @Test
    void findInterfaceByTarget_found() throws InvalidInputException {
        String ip = TEST_IP_PREFIX + "108";
        Target target = dataService.createTarget("TestTargetFind", validInterfaceName, ip);

        Optional<Interface> found = dataService.findInterfaceByTarget(target);
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(testInterface);

        dataService.remove(target);
    }

    @Test
    void findInterfaceByTarget_notFound() {
        Target fakeTarget = new Target("FakeTarget", TEST_IP_PREFIX + "199");
        Optional<Interface> found = dataService.findInterfaceByTarget(fakeTarget);
        assertThat(found).isEmpty();
    }

    @Test
    void removeTarget_success() throws InvalidInputException {
        String ip = TEST_IP_PREFIX + "109";
        Target target = dataService.createTarget("TestTargetRemove", validInterfaceName, ip);

        assertThat(testInterface.getGateway().getDevices()).contains(target);

        dataService.remove(target);

        assertThat(testInterface.getGateway().getDevices()).doesNotContain(target);
    }

    @Test
    void removeNullTarget_noException() {
        dataService.remove(null);
    }

    @Test
    void removeNonTargetDevice_noEffect() {
        // Only Target can be removed, so removing a non-Target should do nothing and not throw
        dataService.remove(testInterface.getGateway());
        assertThat(testInterface.getGateway()).isNotNull();
    }

    @Test
    void interfacesList_isNotNull() {
        List<Interface> interfaces = dataService.getSelfDevice().getAnInterfaces();
        assertThat(interfaces).isNotNull();
    }
}
