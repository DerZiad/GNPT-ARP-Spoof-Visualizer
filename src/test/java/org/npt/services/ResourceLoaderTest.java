package org.npt.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@DisplayName("ResourceLoader File Reading Tests")
public class ResourceLoaderTest {

    @Test
    @DisplayName("Should correctly read content from resource file")
    public void testResourceLoader() throws IOException {
        // given
        String resourceName = "usedForTest.txt";
        String expectedContent = "TEST";
        ResourceLoader resourceLoader = ResourceLoader.getInstance();

        // when
        InputStream is = resourceLoader.getResource(resourceName);
        String contentFromFile = new BufferedReader(new InputStreamReader(is)).readLine();

        // then
        Assertions.assertThat(contentFromFile).isEqualTo(expectedContent);
    }
}
