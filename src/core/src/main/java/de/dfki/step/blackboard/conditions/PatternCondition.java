package de.dfki.step.blackboard.conditions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import de.dfki.step.blackboard.Condition;
import de.dfki.step.blackboard.Token;
import de.dfki.step.blackboard.patterns.Pattern;

/**
 * This class can be used to define conditions declaratively based on the semantic tree.
 * The given {@link Pattern} defines which tokens are matched by the condition.
 */
public class PatternCondition extends Condition {
	private final Pattern _pattern;
	
	public PatternCondition(Pattern p) {
        this.setNumberOfTokens(1);
		this._pattern = p;
	}
	
	@Override
	public List<Token[]> generateMatches(Stream<Token> tokens, List<String> ignoreTags, UUID ignoreUUID) {
        LinkedList<Token[]> result = new LinkedList<>();
        tokens = tokens.filter(c -> !c.isCheckedBy(this.getUUID()) && !c.isUsedBy(ignoreUUID));
		for (Iterator<Token> it = tokens.iterator(); it.hasNext(); )
        {
            Token tok = it.next();

            if(_pattern.matches(tok)) {
                if(result.size() < this.getMaxMatches())
                    result.add(new Token[]{tok});
                else
                    // max Matches generated
                    break;
            }
            tok.checkedBy(this.getUUID());
        }

        return result;
	}

}
