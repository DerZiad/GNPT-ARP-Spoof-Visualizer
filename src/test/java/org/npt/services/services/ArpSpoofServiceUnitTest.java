package org.npt.services.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npt.exception.NotFoundException;
import org.npt.models.Gateway;
import org.npt.models.Target;
import org.npt.services.GraphicalNetworkTracerFactory;
import org.npt.services.defaults.DefaultArpSpoofService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled
class ArpSpoofServiceUnitTest {

    private DefaultArpSpoofService service;
    private Target mockTarget;
    private Gateway mockGateway;

    @BeforeEach
    void setUp() {
        service = (DefaultArpSpoofService) GraphicalNetworkTracerFactory.getInstance().getArpSpoofService();
        mockTarget = mock(Target.class);
        mockGateway = mock(Gateway.class);
    }

    @AfterEach
    void tearDown() {
        service.clear();
    }

    @Test
    void testSingletonInstance() {
        DefaultArpSpoofService another = (DefaultArpSpoofService) GraphicalNetworkTracerFactory.getInstance().getArpSpoofService();
        assertThat(another).isSameAs(service);
    }

    @Test
    void testGetArpSpoofProcessNotExists() {
        when(mockTarget.getIp()).thenReturn("192.168.0.100");
        Optional<DefaultArpSpoofService.ArpSpoofProcess> result = service.getArpSpoofProcess(mockTarget);
        assertThat(result).isEmpty();
    }

    @Test
    void testSpoofSuccess() throws Exception {
        when(mockTarget.getIp()).thenReturn("192.168.0.100");
        when(mockGateway.getIp()).thenReturn("192.168.0.1");

        try (MockedStatic<Runtime> mocked = mockStatic(Runtime.class)) {
            Process process = mock(Process.class);
            Runtime runtime = mock(Runtime.class);
            mocked.when(Runtime::getRuntime).thenReturn(runtime);
            when(runtime.exec(any(String[].class))).thenReturn(process);

            service.spoof("eth0", mockTarget, mockGateway);

            assertThat(service.getArpSpoofProcesses()).hasSize(1);
            var spoof = service.getArpSpoofProcesses().get(0);
            assertThat(spoof.target()).isEqualTo(mockTarget);
            assertThat(spoof.gateway()).isEqualTo(mockGateway);
            assertThat(spoof.tasksThreads()).hasSize(2);
            assertThat(spoof.packetSnifferThreadPair()).isNotNull();
        }
    }

    @Test
    void testSpoofTargetMissingIp() {
        when(mockTarget.getIp()).thenReturn(null);
        assertThatThrownBy(() -> service.spoof("eth0", mockTarget, mockGateway))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testSpoofGatewayMissingIp() {
        when(mockTarget.getIp()).thenReturn("192.168.0.100");
        when(mockGateway.getIp()).thenReturn(null);
        assertThatThrownBy(() -> service.spoof("eth0", mockTarget, mockGateway))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testClearAllProcesses() throws Exception {
        when(mockTarget.getIp()).thenReturn("192.168.0.100");
        when(mockGateway.getIp()).thenReturn("192.168.0.1");

        Target mockTarget2 = mock(Target.class);
        Gateway mockGateway2 = mock(Gateway.class);
        when(mockTarget2.getIp()).thenReturn("192.168.0.101");
        when(mockGateway2.getIp()).thenReturn("192.168.0.1");

        try (MockedStatic<Runtime> mocked = mockStatic(Runtime.class)) {
            Process process = mock(Process.class);
            Runtime runtime = mock(Runtime.class);
            mocked.when(Runtime::getRuntime).thenReturn(runtime);
            when(runtime.exec(any(String[].class))).thenReturn(process);

            service.spoof("eth0", mockTarget, mockGateway);
            service.spoof("eth0", mockTarget2, mockGateway2);

            assertThat(service.getArpSpoofProcesses()).hasSize(2);

            service.clear();

            verify(process, times(4)).destroy();
            assertThat(service.getArpSpoofProcesses()).isEmpty();
        }
    }
}
