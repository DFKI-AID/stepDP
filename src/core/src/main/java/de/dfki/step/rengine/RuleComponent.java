package de.dfki.step.rengine;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import de.dfki.step.core.Clock;
import de.dfki.step.core.ClockComponent;

/**
 * Wraps a {@link RuleSystem} into a component such that other components can access the rules.
 */
public class RuleComponent extends RuleSystem implements Component {

    public RuleComponent(Clock clock) {
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
