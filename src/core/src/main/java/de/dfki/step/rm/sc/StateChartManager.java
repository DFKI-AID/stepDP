package de.dfki.step.rm.sc;

import de.dfki.step.rm.sc.internal.Parser;
import de.dfki.step.blackboard.RuleManager;
import de.dfki.step.rm.sc.internal.SCEngine;
import de.dfki.step.rm.sc.internal.StateChart;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class StateChartManager {

    private StateChart sc;
    private SCEngine scEngine;

    public StateChartManager(URL resource) throws IOException, URISyntaxException {
        this.sc = Parser.loadStateChart(resource);
        this.scEngine = new SCEngine(this.sc);
    }

    public StateChartManager(String resourceStr) throws IOException, URISyntaxException {
        if (resourceStr.endsWith(".scxml")) {
            resourceStr = resourceStr.substring(0, resourceStr.length() - 6);
        }
        if (resourceStr.startsWith("/")) {
            resourceStr = resourceStr.substring(1, resourceStr.length());
        }
        InputStream scStream = StateChartManager.class.getResourceAsStream("/" + resourceStr + ".scxml");
        this.sc = Parser.loadStateChart(scStream);
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

    public SCEngine getEngine() {
    	return this.scEngine;
    }

    /**
     * Generate a Rule Manager based on a state chart
     * @param defaultValue default value of the rule, true is active and false is inactive
     * @param differentStates states in which the default value is inverted
     * @return
     */
    public SCRuleManager getRuleAssignment(boolean defaultValue, String[] differentStates)
    {
        return new SCRuleManager(this, defaultValue, differentStates);
    }

}
