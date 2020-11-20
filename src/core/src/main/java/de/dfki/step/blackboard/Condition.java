package de.dfki.step.blackboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public abstract class Condition {
    private static final Logger log = LoggerFactory.getLogger(Condition.class);

    private int _numberOfTokens;

    /**
     * Number of tokens the condition requires
     * @return
     */
    public int getNumberOfTokens() {
        return _numberOfTokens;
    }

    /**
     * Set the number of tokens the condition requires
     * @param numberOfTokens
     */
    public void setNumberOfTokens(int numberOfTokens) {
        this._numberOfTokens = numberOfTokens;
    }

    public abstract List<Token[]> generatePossibilities(List<Token> tokens, String[] ignoreTags, UUID ignoreUUID);
}
