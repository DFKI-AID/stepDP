package de.dfki.step.blackboard.conditions;

import de.dfki.step.blackboard.Condition;
import de.dfki.step.blackboard.Token;
import de.dfki.step.kb.semantic.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class SingleTypeCondition extends Condition {
    private static final Logger log = LoggerFactory.getLogger(SingleTypeCondition.class);

    private Type _type;

    public SingleTypeCondition(Type type)
    {
        this.setNumberOfTokens(1);
        this._type = type;
    }

    @Override
    public List<Token[]> generatePossibilities(List<Token> tokens, String[] ignoreTags, UUID ignoreUUID) {
        LinkedList<Token[]> result = new LinkedList<>();

        for(Token tok : tokens)
        {
            // check if token is not usable because of ignore tags
            if(tok.getIgnoreRuleTags() != null &&
                    Arrays.stream(tok.getIgnoreRuleTags()).anyMatch(s -> Arrays.asList(ignoreTags).contains(s)))
                continue;

            // check if the token was already consumed by this rule
            if(tok.getUsedBy().contains(ignoreUUID))
                continue;

            // check if token is of the given type or inherit from the type
            if(tok.getType().isInheritanceFrom(this._type))
                result.add(new Token[]{tok});
        }

        return result;
    }
}
