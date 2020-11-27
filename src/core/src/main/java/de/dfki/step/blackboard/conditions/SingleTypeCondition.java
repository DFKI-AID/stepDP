package de.dfki.step.blackboard.conditions;

import de.dfki.step.blackboard.Condition;
import de.dfki.step.blackboard.Token;
import de.dfki.step.kb.semantic.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class SingleTypeCondition extends Condition {
    private static final Logger log = LoggerFactory.getLogger(SingleTypeCondition.class);

    private Type _type;

    public SingleTypeCondition(Type type)
    {
        this.setNumberOfTokens(1);
        this._type = type;
    }

    @Override
    public List<Token[]> generateMatches(Stream<Token> tokens, String[] ignoreTags, UUID ignoreUUID) {
        LinkedList<Token[]> result = new LinkedList<>();

        for (Iterator<Token> it = tokens.iterator(); it.hasNext(); )
        {
            Token tok = it.next();

            // check if token is of the given type or inherit from the type
            if(tok.getType().isInheritanceFrom(this._type)) {
                if(result.size() < this.getMaxMatches())
                    result.add(new Token[]{tok});
                else
                    // max Matches generated
                    break;
            }
        }

        return result;
    }
}
