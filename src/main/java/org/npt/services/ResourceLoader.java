package org.npt.services;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DefaultResourceLoader implements ResourceLoader {

    private static ResourceLoader instance = null;
    private static final String PATH = "/org/npt/%s";

    @Override
    public InputStream getResource(String name) {
        String resourcePath = String.format(PATH, name);
        return this.getClass().getResourceAsStream(resourcePath);
    }

    public static ResourceLoader getInstance() {
        if (instance == null)
            instance = new DefaultResourceLoader();
        return instance;
    }
}

public interface ResourceLoader {

    public InputStream getResource(String name);

    public static ResourceLoader getInstance(){
        return DefaultResourceLoader.getInstance();
    }
}
