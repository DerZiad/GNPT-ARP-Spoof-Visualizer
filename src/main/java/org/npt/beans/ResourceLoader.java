package org.npt.beans;

import java.io.InputStream;

public interface ResourceLoader {

    public InputStream getResource(String name);
}
