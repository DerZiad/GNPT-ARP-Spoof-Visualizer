package org.npt.controllers;

import org.npt.services.GraphicalNetworkTracerFactory;

import lombok.Data;

@Data
public class DataInjector {

    protected static final GraphicalNetworkTracerFactory graphicalNetworkTracerFactory = GraphicalNetworkTracerFactory.getInstance();

    private Object[] args;
}
