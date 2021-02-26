package de.dfki.step.rm.sc;
import de.dfki.step.blackboard.RuleManager;

import java.util.Arrays;

public class SCRuleManager extends RuleManager{

    StateChartManager parent;
    boolean defaultState;
    String[] specialStates;

    public SCRuleManager(StateChartManager parent, boolean defaultState, String[] specialStates)
    {
        this.parent = parent;
        this.defaultState = defaultState;
        this.specialStates = specialStates;
    }

    public void update()
    {

    }

    public boolean isRuleActive()
    {
        String currentState = parent.getCurrentState();

        if(Arrays.stream(specialStates).anyMatch(s -> s.equals(currentState))) {
            return !this.defaultState;
        }
        return this.defaultState;
    }
}
