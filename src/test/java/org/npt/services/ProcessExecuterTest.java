package org.npt.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProcessService Execution and Utility Tests")
public class ProcessExecuterTest {

    @Test
    @DisplayName("Should add executed process to task list with correct details")
    public void testExecutingProcess() {
        // Given
        String command = "echo linux";

        // When
        ProcessService.execute("Test", command.split(" "));

        // Then
        assertThat(ProcessService.tasks.size()).isEqualTo(1);
        assertThat(ProcessService.tasks.getFirst().getProcessName()).isEqualTo("Test");
        assertThat(ProcessService.tasks.getFirst().getCommand()).isEqualTo(command);
    }

    @Test
    @DisplayName("Should generate a different MD5 hash for given string")
    public void testMd5Encryption() {
        // Given
        String message = "Test";

        // When
        String md5 = ProcessService.ProcessUtils.generateProcessNameFrom(message);

        // Then
        assertThat(md5).isNotEqualTo(message);
    }
}
