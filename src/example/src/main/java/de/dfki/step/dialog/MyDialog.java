package de.dfki.step.dialog;


import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.conditions.PatternCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.blackboard.rules.SimpleRule;
import de.dfki.step.core.InputComponent;
import de.dfki.step.core.Schema;
import de.dfki.step.fusion.FusionComponent;
import de.dfki.step.fusion.InputNode;
import de.dfki.step.fusion.ParallelNode;
import de.dfki.step.core.Token;
import de.dfki.step.kb.semantic.Type;
import de.dfki.step.srgs.Grammar;
import de.dfki.step.srgs.GrammarManager;
import de.dfki.step.srgs.MyGrammar;
import de.dfki.step.web.SpeechRecognitionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.*;


/**
 *
 */
public class MyDialog extends Dialog {
    private static final Logger log = LoggerFactory.getLogger(MyDialog.class);
    private MyGrammar appGrammar;
    private final String app = "assemblyrobot";

    public MyDialog() {
        try {
            TaskBehavior taskBehavior = new TaskBehavior();
            this.addComponent(taskBehavior);
            this.addComponent(new SimpleBehavior());

        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not load task behavior", e);
        }

        MetaFactory mf = new MetaFactory(this);
        mf.createUndoRule();

        mf.turnGrabRule("interrupt", (token) -> {
            System.out.println("TODO interrupt output + \"yes?\"");
        });


        mf.createGreetingsRule();

        TimeBehavior timeBehavior = new TimeBehavior();
        this.addComponent(timeBehavior);


        mf.createRepeatRule("request_repeat_tts", "I did not say anything.");
        mf.snapshotRule();


        //initFusionComponent();
        //createGrammarComponent();


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

            de.dfki.step.blackboard.Token test = new de.dfki.step.blackboard.Token(this.getKB());
            test.setType(GreetingIntent);
            this.getBlackboard().addToken(test);

            Rule GreetingRule = new SimpleRule(tokens -> {
                System.out.println("Greeting found! Say hello");

                de.dfki.step.blackboard.Token HelloToken = new de.dfki.step.blackboard.Token(this.getKB());
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

    protected void initFusionComponent() {
        FusionComponent fc = retrieveComponent(FusionComponent.class);
        //addComponent("fusion", fc);

        //looking and pointing gesture may trigger a select_task intent
        InputNode gesture = new InputNode(t -> t.payloadEquals("gesture", "tap"));
        InputNode taskFocus = new InputNode(t ->
                t.get("focus", String.class).map(f -> f.startsWith("task")).orElse(false));

        ParallelNode node = new ParallelNode()
                .add(gesture)
                .add(taskFocus);

        fc.addFusionNode("select_task1", node, match -> {
            List<String> origin = Token.mergeFields("origin", String.class, match.getTokens());
            OptionalDouble confidence = Token.mergeFields("confidence", Double.class, match.getTokens()).stream()
                    .mapToDouble(x -> x).average();
            Optional<String> task = Token.getAny("focus", String.class, match.getTokens());

            Token token = new Token()
                    .add("intent", "select_task")
                    .add("origin", origin)
                    .add("task", task.get());

            if (confidence.isPresent()) {
                token = token.add("confidence", confidence.getAsDouble());
            }

            return token;
        });


        //forward all input tokens that already have an intent
        InputNode intentNode = new InputNode(t -> t.has("intent"));
        fc.addFusionNode("intent_forward", intentNode, match -> {
            return match.getTokens().iterator().next();
        });


        //focus + speech for task selection: "Select this one"
        Schema selectThisSchema = Schema.builder()
                .equals("intent", "specify")
                .equals("specification", "this")
                .build();
        fc.addFusionNode("select_task2", new ParallelNode()
                        .add(new InputNode(selectThisSchema))
                        .add(taskFocus)
                , match -> {
                    Token intent = FusionComponent.defaultIntent(match, "select");
                    intent = intent.add("selection", Token.getAny("focus", String.class, match.getTokens()).get());
                    return intent;
                });
    }

    protected void createGrammarComponent() {
        // speech-recognition-service of the step-dp
        GrammarManager gm = MyGrammar.create();
        Grammar grammar = gm.createGrammar();
        SpeechRecognitionClient src = new SpeechRecognitionClient(app, "localhost", 9696, (token) -> {
            // TODO use resolution_entity on token
            Optional<Map> semantic = token.get(Map.class, "semantic");
            if (!semantic.isPresent()) {
                return;
            }

            // TODO atm. intent is copied from the speech semantics
            Token processedToken = new Token((Map<String, Object>) semantic.get());
//            if(intent.isPresent()) {
//                processedToken = processedToken.add("intent", intent.get());
//            }
            // add token to fc
            InputComponent ic = retrieveComponent(InputComponent.class);
            ic.addToken(processedToken);
        });
        String grammarStr = grammar.toString();
        src.setGrammar("main", grammarStr);
        src.initGrammar();
        src.init();
    }
}

