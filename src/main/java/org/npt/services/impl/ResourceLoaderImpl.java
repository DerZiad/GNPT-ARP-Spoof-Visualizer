package org.npt.services.impl;

import org.npt.services.ResourceLoader;

import java.io.InputStream;

public class ResourceLoaderImpl implements ResourceLoader {

    private static ResourceLoader instance = null;
    private static final String IMAGE_REPOSITORY = "/org/npt/%s";

    @Override
    public InputStream getResource(String name) {
        String resourcePath = String.format(IMAGE_REPOSITORY, name);
        return this.getClass().getResourceAsStream(resourcePath);
    }

    public static ResourceLoader getInstance() {
        if (instance == null)
            instance = new ResourceLoaderImpl();
        return instance;
    }
}
