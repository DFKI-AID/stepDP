package de.dfki.step.dialog;


import de.dfki.step.blackboard.IToken;
import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.conditions.PatternCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.blackboard.rules.SimpleRule;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IKBObjectWriteable;
import de.dfki.step.kb.semantic.PropInt;
import de.dfki.step.kb.semantic.PropString;
import de.dfki.step.kb.semantic.Type;
import de.dfki.step.web.Controller;
import de.dfki.step.web.webchat.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 */
public class MyDialog extends Dialog {
    private static final Logger log = LoggerFactory.getLogger(MyDialog.class);

    public MyDialog() {
        try {
            Type UserIntent = new Type("UserIntent", this.getKB());
            this.getKB().addType(UserIntent);

            Type GreetingIntent = new Type("GreetingIntent", this.getKB());
            GreetingIntent.addInheritance(UserIntent);
            this.getKB().addType(GreetingIntent);

            Type Bottle = new Type("Bottle", this.getKB());
            Bottle.addProperty(new PropString("material", this.getKB()));
            this.getKB().addType(Bottle);

            Type GlasBottle = new Type("GlasBottle", this.getKB());
            GlasBottle.addInheritance(Bottle);
            this.getKB().addType(GlasBottle);

            Type PlasticBottle = new Type("PlasticBottle", this.getKB());
            PlasticBottle.addInheritance(Bottle);
            this.getKB().addType(PlasticBottle);

            IKBObjectWriteable Bottle42 = this.getKB().createInstance("Bottle42", GlasBottle);
            Bottle42.setString("material", "glas");
            Bottle42.setInteger( "size",500);
            Bottle42.setString("color", "green");
            Bottle42.setString("mfn", "Mfn1");
            Bottle42.setString("location", "Storage3");

            de.dfki.step.blackboard.BasicToken test = new de.dfki.step.blackboard.BasicToken(this.getKB());
            test.setType(GreetingIntent);
            this.getBlackboard().addToken(test);

            Rule GreetingRule = new SimpleRule(tokens -> {
                System.out.println("Greeting found! Say hello");
            }, "GreetingRule");
            Pattern p = new PatternBuilder("GreetingIntent", this.getKB()).build();
            GreetingRule.setCondition(new PatternCondition(p));
            GreetingRule.getCondition().setMinTokenAge(1 * 1000);
            GreetingRule.getCondition().setMaxTokenAge(30 * 1000);
            this.getBlackboard().addRule(GreetingRule);


            Rule TestRule = new SimpleRule(tokens -> {
                IKBObject[] testArray = tokens[0].getResolvedReferenceArray("test");
                System.out.println("Greeting found! Say hello");
            }, "GreetingRule");
            Pattern p2 = new PatternBuilder("PlasticBottle", this.getKB()).build();
            TestRule.setCondition(new PatternCondition(p2));
            this.getBlackboard().addRule(TestRule);

            try {
                Controller.webChatInputType = new Type("WebChatInputType", this.getKB());
                Controller.webChatInputType.addProperty(new PropInt("session", this.getKB()));
                Controller.webChatInputType.addProperty(new PropString("userText", this.getKB()));
                this.getKB().addType(Controller.webChatInputType);
            } catch (Exception e) {
                System.out.println("WebChatInputType could not be initialized");
                e.printStackTrace();
            }

            Rule ChatEchoRule = new SimpleRule(tokens -> {
                IToken t = tokens[0];

                Controller.webChat.addMessage("session", "bot", t.getString("userText"));
            }, "ChatEchoRule");
            Pattern p3 = new PatternBuilder("WebChatInputType", this.getKB()).build();
            ChatEchoRule.setCondition(new PatternCondition(p3));
            this.getBlackboard().addRule(ChatEchoRule);

            Controller.addExampleToken("add custom intent", "{\"type\": \"CustomIntent\", \"userName\":\"Alice\"}");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void update() {
        super.update();
    }
}
