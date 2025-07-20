package org.npt.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Target Model Unit Tests")
class TargetUnitTest {

    @Test
    @DisplayName("Constructor sets fields correctly")
    void constructor_setsFieldsCorrectly() {
        Target target = new Target("t1", "192.168.1.10");
        assertThat(target.getDeviceName()).isEqualTo("t1");
        assertThat(target.getIp()).isEqualTo("192.168.1.10");
    }

    @Test
    @DisplayName("Equals and hashCode work as expected")
    void equalsAndHashCode_work() {
        Target t1 = new Target("t1", "192.168.1.10");
        Target t2 = new Target("t1", "192.168.1.10");
        assertThat(t1).isNotEqualTo(t2); // different keys
        assertThat(t1.hashCode()).isNotEqualTo(t2.hashCode());
    }
}
