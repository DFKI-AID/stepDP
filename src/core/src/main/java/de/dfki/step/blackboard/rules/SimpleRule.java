package de.dfki.step.blackboard.rules;

import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.Token;

import java.util.List;
import java.util.function.Consumer;

public class SimpleRule extends Rule {

    protected SimpleRuleInterface _function;

    /**
     * If suitable tokens are found, the first best combination is called
     * @param function
     */
    public SimpleRule(SimpleRuleInterface function)
    {
        this._function = function;
    }

    @Override
    public void onMatch(List<Token[]> tokens)
    {
        if(tokens == null || tokens.size() == 0)
            return;

        // take the first best combination...
        Token[] firstOne = tokens.get(0);

        // .. and mark that this rule used it!
        for(int i = 0; i < firstOne.length; i++)
            firstOne[i].usedBy(this.getUUID());

        // do smth clever here with the token
        this._function.onMatch(firstOne);
    }
}
