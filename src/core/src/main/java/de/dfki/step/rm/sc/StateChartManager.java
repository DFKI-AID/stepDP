package de.dfki.step.rm.sc;

import de.dfki.step.rm.sc.internal.Parser;
import de.dfki.step.blackboard.RuleManager;
import de.dfki.step.rm.sc.internal.SCEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class StateChartManager {

    private de.dfki.step.rm.sc.internal.StateChart sc;
    private de.dfki.step.rm.sc.internal.SCEngine scEngine;

    public StateChartManager(URL resource) throws IOException, URISyntaxException {
        this.sc = Parser.loadStateChart(resource);
        this.scEngine = new SCEngine(this.sc);
    }

    public void fireTransition(String transition)
    {
        this.scEngine.fire(transition);
    }

    public String getCurrentState()
    {
        return this.scEngine.getCurrentState();
    }

    /**
     * Generate a Rule Manager based on a state chart
     * @param defaultValue default value of the rule, true is active and false is inactive
     * @param differentStates states in which the default value is inverted
     * @return
     */
    public StateChartRuleManager getRuleAssignment(boolean defaultValue, String[] differentStates)
    {
        return new StateChartRuleManager(this, defaultValue, differentStates);
    }

}
