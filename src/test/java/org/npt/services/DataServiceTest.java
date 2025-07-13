package org.npt.services;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import org.junit.jupiter.api.*;
import org.npt.services.defaults.DefaultDataService;
import org.npt.models.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DataService Behavior Tests")
public class DataServiceTest {

    private static final String TEST_IP = "127.0.0.1";
    private static final String TEST_DEVICE_NAME = "Test";
    private static final String TEST_DEVICE_INTERFACE = "testeth0";

    private static DataService dataService;

    @BeforeAll
    public static void setup() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        dataService = DefaultDataService.getInstance();
        dataService.run();
        dataService.clear();
    }

    @AfterEach
    public void clean() {
        dataService.clear();
    }

    @Test
    @DisplayName("Should add device and throw NPE when adding null device")
    public void testAddDeviceForTarget() {
        // Given
        Target target = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), 0, 0, new ContextMenu());

        // When
        dataService.addDevice(Optional.of(target));

        // Then
        assertThatThrownBy(() -> dataService.addDevice(Optional.empty()))
                .isInstanceOf(NullPointerException.class);
        assertThat(dataService.getDevices()).contains(target);
    }

    @Test
    @DisplayName("Should remove device by object and handle null case")
    public void testRemoveByObject() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), 0, 0, new ContextMenu());
        dataService.addDevice(Optional.of(target));
        dataService.removeByObject(Optional.of(target));

        assertThatThrownBy(() -> dataService.removeByObject(Optional.empty()))
                .isInstanceOf(NullPointerException.class);
        assertThat(dataService.getDevices()).doesNotContain(target);
    }

    @Test
    @DisplayName("Should remove device by index and throw on null index")
    public void testRemoveByIndex() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), 0, 0, new ContextMenu());
        dataService.addDevice(Optional.of(target));
        dataService.removeByIndex(Optional.of(0));

        assertThatThrownBy(() -> dataService.removeByIndex(Optional.empty()))
                .isInstanceOf(NullPointerException.class);
        assertThat(dataService.getDevices()).doesNotContain(target);
    }

    @Test
    @DisplayName("Should get device by index or return empty if not found")
    public void testGetDevice() {
        Target target = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), 0, 0, new ContextMenu());
        dataService.addDevice(Optional.of(target));

        assertThatThrownBy(() -> dataService.getDevice(Optional.empty()))
                .isInstanceOf(NullPointerException.class);
        assertThat(dataService.getDevice(Optional.of(0))).isPresent();
        assertThat(dataService.getDevice(Optional.of(100))).isEmpty();
    }

    @Test
    @DisplayName("Should filter devices by class type")
    public void testGetDevicesByClass() {
        Target t1 = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), 0, 0, new ContextMenu());
        Target t2 = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), 0, 0, new ContextMenu());
        Target t3 = new Target(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), 0, 0, new ContextMenu());
        Gateway g1 = new Gateway(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), 0, 0, new ContextMenu(), List.of(t1, t2));
        Gateway g2 = new Gateway(TEST_DEVICE_NAME, TEST_DEVICE_INTERFACE, List.of(TEST_IP), 0, 0, new ContextMenu(), List.of(t3));

        dataService.addDevice(Optional.of(t1));
        dataService.addDevice(Optional.of(t2));
        dataService.addDevice(Optional.of(t3));
        dataService.addDevice(Optional.of(g1));
        dataService.addDevice(Optional.of(g2));

        assertThatThrownBy(() -> dataService.getDevices(Optional.of(null)))
                .isInstanceOf(NullPointerException.class);
        assertThat(dataService.getDevices(Optional.of(Target.class)).size()).isEqualTo(3);
        assertThat(dataService.getDevices(Optional.of(Gateway.class)).size()).isEqualTo(2);
    }
}
