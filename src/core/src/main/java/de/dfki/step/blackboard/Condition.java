package de.dfki.step.blackboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class Condition {
    private static final Logger log = LoggerFactory.getLogger(Condition.class);

    private int _numberOfTokens;
    private int _maxMatches = 10;
    private final UUID _uuid = UUID.randomUUID();
    private long _maxTokenAge = DEFAULT_MAX_TOKEN_AGE;
    private long _minTokenAge = DEFAULT_MIN_TOKEN_AGE;

    public final static long DEFAULT_MAX_TOKEN_AGE = Duration.ofHours(1).toMillis();
    public final static long DEFAULT_MIN_TOKEN_AGE = 0;

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
    
    public UUID getUUID()
    {
        return _uuid;
    }

    public long getMaxTokenAge() {
        return this._maxTokenAge;
    }

    public void setMaxTokenAge(long maxTokenAge) {
        this._maxTokenAge = maxTokenAge;
    }

    public long getMinTokenAge() {
        return this._minTokenAge;
    }

    /**
     * Set min token age (Millisecs) 
     **/
    public void setMinTokenAge(long minTokenAge) {
        this._minTokenAge = minTokenAge;
    }

    public abstract List<IToken[]> generateMatches(Stream<IToken> tokens, List<String> ignoreTags, UUID ignoreUUID);
}
