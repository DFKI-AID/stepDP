package de.dfki.step.blackboard;

import de.dfki.step.blackboard.rules.SimpleRule;
import de.dfki.step.blackboard.rules.SimpleRuleInterface;
import org.junit.Test;

public class BlackboardTest {

    @Test
    public void SimpleRuleTest()
    {
        // SimpleRule with Interface
        SimpleRuleInterface TokenProcessing = (tokens) -> { System.out.println(tokens[0].get("Hallo").get()); };
        Rule rule = new SimpleRule(TokenProcessing);

        // SimpleRule with lambda expression
        Rule rule2 = new SimpleRule(tokens -> {
            System.out.println(tokens[0].get("Hallo").get());
        });
    }

}
