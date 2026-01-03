package org.npt.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SelfDevice Model Unit Tests")
class SelfDeviceUnitTest {

    @Test
    @DisplayName("Constructor sets fields correctly")
    void constructor_setsFieldsCorrectly() {
        SelfDevice self = new SelfDevice("self");
        assertThat(self.getDeviceName()).isEqualTo("self");
        assertThat(self.getAnInterfaces()).isEmpty();
    }

    @Test
    @DisplayName("addInterface adds a single interface.png")
    void addInterface_addsSingleInterface() {
        SelfDevice self = new SelfDevice("self");
        Interface iface = new Interface("eth0", "192.168.1.2", "255.255.255.0", null);
        self.addInterface(iface);
        assertThat(self.getAnInterfaces()).contains(iface);
    }

    @Test
    @DisplayName("addInterfaces adds multiple interfaces")
    void addInterfaces_addsMultipleInterfaces() {
        SelfDevice self = new SelfDevice("self");
        Interface i1 = new Interface("eth1", "192.168.1.3", "255.255.255.0", null);
        Interface i2 = new Interface("eth2", "192.168.1.4", "255.255.255.0", null);
        self.addInterfaces(List.of(i1, i2));
        assertThat(self.getAnInterfaces()).contains(i1, i2);
    }

    @Test
    @DisplayName("getInterfaceIfExist returns correct Optional")
    void getInterfaceIfExist_returnsCorrectOptional() {
        SelfDevice self = new SelfDevice("self");
        Interface iface = new Interface("eth3", "192.168.1.5", "255.255.255.0", null);
        self.addInterface(iface);
        assertThat(self.getInterfaceIfExist("eth3")).contains(iface);
        assertThat(self.getInterfaceIfExist("notfound")).isEmpty();
    }
}
