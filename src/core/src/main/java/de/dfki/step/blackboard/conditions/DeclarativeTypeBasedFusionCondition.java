package de.dfki.step.blackboard.conditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import de.dfki.step.blackboard.Condition;
import de.dfki.step.blackboard.rules.DeclarativeTypeBasedFusionRule;
import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.patterns.Pattern;

/**
 * Condition that the {@link DeclarativeTypeBasedFusionRule} uses.
 */
public class DeclarativeTypeBasedFusionCondition extends Condition {
	private Pattern _p1;
	private Pattern _p2;
	private long _fusionInterval;
	
	public DeclarativeTypeBasedFusionCondition(Pattern p1, Pattern p2, long fusionInterval) {
		this._p1 = p1;
		this._p2 = p2;
		this._fusionInterval = fusionInterval;
		this.setNumberOfTokens(2);
	}

	// Currently, this method only returns the most recent combination of matching tokens.
	// In the future, an option (e.g. configured by an additional constructor parameter) could
	// be added to generate more combinations. However, make sure to avoid duplicate combinations!
	@Override
	public List<BasicToken[]> generateMatches(Stream<BasicToken> tokens, List<String> ignoreTags, UUID ignoreUUID) {
		BasicToken[] match = new BasicToken[2];
		ArrayList<BasicToken[]> result = new ArrayList<BasicToken[]>();
        boolean oneTokenFound = false;
        // fusion interval becomes only relevant when one matching token was found
        long intervalStart = -1;

        // the stream is ordered (newer tokens come first)
		for (Iterator<BasicToken> it = tokens.iterator(); it.hasNext(); )
        {
            BasicToken tok = it.next();
            
            if (breakingConditionMet(oneTokenFound, intervalStart, tok, ignoreUUID))
            	break;

			tok.checkedBy(this.getUUID());

            if(match[0] == null && _p1.matches(tok)) {
            	match[0] = tok;
            	oneTokenFound = true;
            	intervalStart = tok.getTimestamp() - _fusionInterval;
            }
            else if(match[1] == null &&_p2.matches(tok)) {
            	match[1] = tok;
            	oneTokenFound = true;
        		intervalStart = tok.getTimestamp() - _fusionInterval;
            }

            if (match[0] != null && match[1] != null) {
            	result.add(match);
                // return only the most recent match
            	break;
            }
        }
        return result;
	}
	
	private boolean breakingConditionMet(boolean oneTokenFound, long intervalStart, BasicToken tok, UUID ruleUUID) {
        // break if no new token found and current token already checked in last iteration
        if (!oneTokenFound) {
        	if (tok.isCheckedBy(this.getUUID())) 
        			return true;
        }
        // break if one new token found and current token already used for fusion
        // this is a heuristic to avoid older, unused inputs to appear later in the dialog
        // might be adjusted in the future when allowing tokens to be part of multiple combinations
        else if (tok.isUsedBy(ruleUUID)) {
        	return true;
        }
        // break if one new token found but current token already outside of fusion interval
        else if (tok.getTimestamp() < intervalStart) {
        	return true;
        }
        return false;
	}

}
