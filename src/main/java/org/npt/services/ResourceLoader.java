package org.npt.services;

import java.io.InputStream;

public interface ResourceLoader {

    public InputStream getResource(String name);
}
