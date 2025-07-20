package org.npt.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gateway Model Unit Tests")
class GatewayUnitTest {

    @Test
    @DisplayName("Constructor sets fields correctly")
    void constructor_setsFieldsCorrectly() {
        Gateway gateway = new Gateway("gw1", "192.168.1.1");
        assertThat(gateway.getDeviceName()).isEqualTo("gw1");
        assertThat(gateway.getIp()).isEqualTo("192.168.1.1");
        assertThat(gateway.getDevices()).isEmpty();
    }

    @Test
    @DisplayName("Devices list add and remove Target")
    void devicesList_addAndRemoveTarget() {
        Gateway gateway = new Gateway("gw2", "192.168.1.2");
        Target t1 = new Target("t1", "192.168.1.10");
        gateway.getDevices().add(t1);
        assertThat(gateway.getDevices()).contains(t1);
        gateway.getDevices().remove(t1);
        assertThat(gateway.getDevices()).doesNotContain(t1);
    }

    @Test
    @DisplayName("Equals and hashCode work as expected")
    void equalsAndHashCode_work() {
        Gateway g1 = new Gateway("gw", "192.168.1.1");
        Gateway g2 = new Gateway("gw", "192.168.1.1");
        assertThat(g1).isNotEqualTo(g2); // different keys
        assertThat(g1.hashCode()).isNotEqualTo(g2.hashCode());
    }
}
