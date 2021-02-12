package de.dfki.step.blackboard.rules;

import de.dfki.step.blackboard.IToken;

public interface SimpleRuleInterface {

    void onMatch(IToken[] tokens);
}
