package de.dfki.step.blackboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class Condition {
    private static final Logger log = LoggerFactory.getLogger(Condition.class);

    private int _numberOfTokens;
    private int _maxMatches = 10;

    /**
     * Get the number of maximal Matches that get produced
     * @return
     */
    public int getMaxMatches()
    {
        return _maxMatches;
    }

    /**
     * Set the maximal number of Matches that are generated
     * @param maxMatches
     * @return
     */
    public void setMaxMatches(int maxMatches)
    {
        this._maxMatches = maxMatches;
    }

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

    public abstract List<Token[]> generateMatches(Stream<Token> tokens, List<String> ignoreTags, UUID ignoreUUID);
}
