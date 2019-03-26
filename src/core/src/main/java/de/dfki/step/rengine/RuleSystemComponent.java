package de.dfki.step.rengine;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import de.dfki.step.util.Clock;
import de.dfki.step.util.ClockComponent;

public class RuleSystemComponent extends RuleSystem implements Component {

    public RuleSystemComponent(Clock clock) {
        super(clock);
    }

    @Override
    public void init(ComponentManager cm) {
        ClockComponent cc = cm.retrieveComponent(ClockComponent.class);
        this.setClock(cc.getClock());
    }

    @Override
    public void deinit() {

    }

    public RuleSystem getRuleSystem() {
        return this;
    }
}
