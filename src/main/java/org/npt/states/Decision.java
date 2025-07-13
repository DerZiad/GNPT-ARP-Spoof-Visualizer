package org.npt.states;

import java.util.List;

public abstract class Decision extends Step {

    public abstract List<Step> makeDecision();
}
