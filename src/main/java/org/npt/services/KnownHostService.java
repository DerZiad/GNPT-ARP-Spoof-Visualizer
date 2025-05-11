package org.npt.services;

import org.npt.models.KnownHost;

import java.util.HashMap;
import java.util.List;

public interface KnownHostService {

    public HashMap<String, KnownHost> getKnownHosts();
}
