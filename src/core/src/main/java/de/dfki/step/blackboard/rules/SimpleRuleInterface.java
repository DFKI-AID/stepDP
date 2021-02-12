package de.dfki.step.blackboard.rules;

import de.dfki.step.blackboard.BasicToken;

public interface SimpleRuleInterface {

    void onMatch(BasicToken[] tokens);
}
