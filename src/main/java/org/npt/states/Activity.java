package org.npt.states;

import org.npt.exception.ShutdownException;

public abstract class Activity extends Step {

    public abstract void initialize() throws ShutdownException;

    public abstract boolean shouldBeInitialized();

    public boolean mustRestart() {
        return false;
    }

    public void restart() {

    }

    public void stop() {

    }
}
