package de.dfki.step.blackboard.rules;

import de.dfki.step.blackboard.Board;
import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.BasicToken;

import java.util.List;
import java.util.function.Consumer;

public class SimpleRule extends Rule {

    protected SimpleRuleInterface _function;
    protected String _name;
    protected final static int DEFAULT_PRIO = 10000;

    /**
     * If suitable tokens are found, the first best combination is called
     * @param function
     */
    public SimpleRule(SimpleRuleInterface function)
    {
        this(function, "SimpleRule");
    }

    public SimpleRule(SimpleRuleInterface function, String name)
    {
    	this.setPriority(DEFAULT_PRIO);
        this._function = function;
        this.setName(name);
    }

    @Override
    public void onMatch(List<BasicToken[]> tokens, Board board)
    {
        if(tokens == null || tokens.size() == 0)
            return;

        // take the first best combination...
        BasicToken[] firstOne = tokens.get(0);

        // .. and mark that this rule used it!
        for(int i = 0; i < firstOne.length; i++)
            firstOne[i].usedBy(this.getUUID());

        // do smth clever here with the token
        this._function.onMatch(firstOne);
    }
}
