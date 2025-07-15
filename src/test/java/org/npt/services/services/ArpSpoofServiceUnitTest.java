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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
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
        DefaultArpSpoofService anotherInstance = (DefaultArpSpoofService) GraphicalNetworkTracerFactory.getInstance().getArpSpoofService();
        assertSame(service, anotherInstance, "Should return the same instance");
    }

    @Test
    void testGetArpSpoofProcessWhenNotExists() {
        when(mockTarget.getIpAddresses()).thenReturn(List.of("192.168.1.100"));
        Optional<DefaultArpSpoofService.ArpSpoofProcess> result = service.getArpSpoofProcess(mockTarget);
        assertFalse(result.isPresent(), "Should return empty optional when process doesn't exist");
    }

    @Test
    void testSpoofHappyPath() throws NotFoundException, IOException {
        // Mock target and gateway responses
        when(mockTarget.findFirstIPv4()).thenReturn(Optional.of("192.168.1.100"));
        when(mockGateway.findFirstIPv4()).thenReturn(Optional.of("192.168.1.1"));

        try (MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)) {
            // Mock runtime execution
            Process mockProcess = mock(Process.class);
            Runtime mockRuntime = mock(Runtime.class);
            when(Runtime.getRuntime()).thenReturn(mockRuntime);
            when(mockRuntime.exec(any(String[].class))).thenReturn(mockProcess);

            // Execute the test
            service.spoof("eth0", mockTarget, mockGateway);

            // Verify results
            assertEquals(1, service.getArpSpoofProcesses().size(), "Should have one process");
            DefaultArpSpoofService.ArpSpoofProcess process = service.getArpSpoofProcesses().get(0);
            assertEquals(mockTarget, process.target());
            assertEquals(mockGateway, process.gateway());
            assertEquals(2, process.tasksThreads().size(), "Should have two tasks (bidirectional spoofing)");
            assertNotNull(process.packetSnifferThreadPair(), "Should have packet sniffer thread");
        }
    }

    @Test
    void testSpoofWhenTargetHasNoIPv4() {
        when(mockTarget.findFirstIPv4()).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            service.spoof("eth0", mockTarget, mockGateway);
        }, "Should throw when target has no IPv4");
    }

    @Test
    void testSpoofWhenGatewayHasNoIPv4() {
        when(mockTarget.findFirstIPv4()).thenReturn(Optional.of("192.168.1.100"));
        when(mockGateway.findFirstIPv4()).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            service.spoof("eth0", mockTarget, mockGateway);
        }, "Should throw when gateway has no IPv4");
    }

    @Test
    void testStopProcess() throws NotFoundException, IOException {
        // Mock target and gateway responses
        when(mockTarget.findFirstIPv4()).thenReturn(Optional.of("192.168.1.100"));
        when(mockGateway.findFirstIPv4()).thenReturn(Optional.of("192.168.1.1"));

        try (MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)) {
            // Mock runtime execution
            Process mockProcess = mock(Process.class);
            Runtime mockRuntime = mock(Runtime.class);
            when(Runtime.getRuntime()).thenReturn(mockRuntime);
            when(mockRuntime.exec(any(String[].class))).thenReturn(mockProcess);

            // Start the spoofing
            service.spoof("eth0", mockTarget, mockGateway);

            // Now stop it
            //service.stop(mockTarget, mockGateway);

            // Verify process was stopped
            verify(mockProcess, times(2)).destroy(); // Called for each direction
            assertTrue(service.getArpSpoofProcesses().isEmpty(), "Process list should be empty after stop");
        }
    }

    @Test
    void testStopNonExistentProcess() {
        when(mockTarget.findFirstIPv4()).thenReturn(Optional.of("192.168.1.100"));
        when(mockGateway.findFirstIPv4()).thenReturn(Optional.of("192.168.1.1"));

        assertThrows(NotFoundException.class, () -> {
            //service.stop(mockTarget, mockGateway);
        }, "Should throw when trying to stop non-existent process");
    }

    @Test
    void testClearAllProcesses() throws NotFoundException, IOException {
        // Mock target and gateway responses
        when(mockTarget.findFirstIPv4()).thenReturn(Optional.of("192.168.1.100"));
        when(mockGateway.findFirstIPv4()).thenReturn(Optional.of("192.168.1.1"));

        try (MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)) {
            // Mock runtime execution
            Process mockProcess = mock(Process.class);
            Runtime mockRuntime = mock(Runtime.class);
            when(Runtime.getRuntime()).thenReturn(mockRuntime);
            when(mockRuntime.exec(any(String[].class))).thenReturn(mockProcess);

            // Start multiple spoofing processes
            Target mockTarget2 = mock(Target.class);
            Gateway mockGateway2 = mock(Gateway.class);
            when(mockTarget2.findFirstIPv4()).thenReturn(Optional.of("192.168.1.101"));
            when(mockGateway2.findFirstIPv4()).thenReturn(Optional.of("192.168.1.1"));

            service.spoof("eth0", mockTarget, mockGateway);
            service.spoof("eth0", mockTarget2, mockGateway2);

            // Verify we have 2 processes
            assertEquals(2, service.getArpSpoofProcesses().size());

            // Clear all
            service.clear();

            // Verify all processes were stopped and cleared
            verify(mockProcess, times(4)).destroy(); // 2 processes × 2 directions each
            assertTrue(service.getArpSpoofProcesses().isEmpty());
        }
    }
}