package org.npt.controllers;

import lombok.Data;
import org.npt.services.*;

@Data
public class DataInjector {

    protected static final GraphicalNetworkTracerFactory graphicalNetworkTracerFactory = GraphicalNetworkTracerFactory.getInstance();

    private Object[] args;
}
