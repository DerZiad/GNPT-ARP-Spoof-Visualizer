package org.npt.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Interface Model Unit Tests")
class InterfaceUnitTest {

    @Test
    @DisplayName("Constructor sets fields correctly")
    void constructor_setsFieldsCorrectly() {
        Gateway gw = new Gateway("gw", "192.168.1.1");
        Interface iface = new Interface("eth0", "192.168.1.2", "255.255.255.0", gw);
        assertThat(iface.getDeviceName()).isEqualTo("eth0");
        assertThat(iface.getIp()).isEqualTo("192.168.1.2");
        assertThat(iface.getNetmask()).isEqualTo("255.255.255.0");
        assertThat(iface.getGateway()).isEqualTo(gw);
    }

    @Test
    @DisplayName("targetAlreadyScanned works by Target and by IP")
    void targetAlreadyScanned_byTarget_and_byIp() {
        Gateway gw = new Gateway("gw", "192.168.1.1");
        Interface iface = new Interface("eth1", "192.168.1.3", "255.255.255.0", gw);
        Target t1 = new Target("t1", "192.168.1.10");
        gw.getDevices().add(t1);

        assertThat(iface.targetAlreadyScanned(t1)).isTrue();
        assertThat(iface.targetAlreadyScanned("192.168.1.10")).isTrue();
        assertThat(iface.targetAlreadyScanned("192.168.1.99")).isFalse();
    }

    @Test
    @DisplayName("targetAlreadyScanned returns false if no gateway")
    void targetAlreadyScanned_returnsFalseIfNoGateway() {
        Interface iface = new Interface("eth2", "192.168.1.4", "255.255.255.0", null);
        Target t2 = new Target("t2", "192.168.1.20");
        assertThat(iface.targetAlreadyScanned(t2)).isFalse();
        assertThat(iface.targetAlreadyScanned("192.168.1.20")).isFalse();
    }
}
