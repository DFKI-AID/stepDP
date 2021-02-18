package de.dfki.step.dialog;


import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.conditions.PatternCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.blackboard.rules.SimpleRule;
import de.dfki.step.kb.semantic.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 */
public class MyDialog extends Dialog {
    private static final Logger log = LoggerFactory.getLogger(MyDialog.class);

    public MyDialog() {
        try {
            Type GreetingIntent = new Type("GreetingIntent", this.getKB());
            GreetingIntent.addInheritance(this.getKB().getType("Token"));
            Type GreetingSpecificIntent = new Type("GreetingSpecificIntent", this.getKB());
            GreetingSpecificIntent.addInheritance(GreetingIntent);
            Type HelloIntent = new Type("HelloIntent", this.getKB());
            HelloIntent.addInheritance(this.getKB().getType("Token"));

            this.getKB().addType(GreetingIntent);
            this.getKB().addType(GreetingSpecificIntent);
            this.getKB().addType(HelloIntent);

            de.dfki.step.blackboard.BasicToken test = new de.dfki.step.blackboard.BasicToken(this.getKB());
            test.setType(GreetingIntent);
            this.getBlackboard().addToken(test);

            Rule GreetingRule = new SimpleRule(tokens -> {
                System.out.println("Greeting found! Say hello");

                de.dfki.step.blackboard.BasicToken HelloToken = new de.dfki.step.blackboard.BasicToken(this.getKB());
                HelloToken.setType(HelloIntent);
                this.getBlackboard().addToken(HelloToken);
            }, "GreetingRule");
            Pattern p = new PatternBuilder("GreetingIntent", this.getKB()).build();
            GreetingRule.setCondition(new PatternCondition(p));
            this.getBlackboard().addRule(GreetingRule);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void update() {
        super.update();
    }
}

