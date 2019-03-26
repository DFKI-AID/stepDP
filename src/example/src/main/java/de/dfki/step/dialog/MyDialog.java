package de.dfki.step.dialog;


import de.dfki.step.fusion.FusionComponent;
import de.dfki.step.fusion.InputNode;
import de.dfki.step.fusion.ParallelNode;
import de.dfki.step.rengine.Token;
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
            this.addComponent("task_behavior", taskBehavior);

        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not load task behavior", e);
        }

        MetaFactory mf = new MetaFactory(this);
        mf.createUndoRule();

        mf.turnGrabRule( "interrupt", (token) -> {
            System.out.println("TODO interrupt output + \"yes?\"");
        });


        mf.createGreetingsRule();

        TimeBehavior timeBehavior = new TimeBehavior();
        this.addComponent("time_behavior", timeBehavior);


        mf.createRepeatRule("request_repeat_tts", "I did not say anything.");
        mf.snapshotRule();


        createFusionComponent();
        createGrammarComponent();
    }


    @Override
    public void update() {
        super.update();
    }

    protected void createFusionComponent() {
        FusionComponent fc = new FusionComponent();
        addComponent("fusion", fc);

        //looking and pointing gesture may trigger a select_task intent
        InputNode gesture = new InputNode(t -> t.payloadEquals("gesture", "tap"));
        InputNode focus = new InputNode(t ->
                t.get("focus", String.class).map(f -> f.startsWith("task")).orElse(false));

        ParallelNode node = new ParallelNode()
                .add(gesture)
                .add(focus);


        fc.addFusionNode("select_task1", node, match -> {
            List<String> origin = Token.mergeFields("origin", String.class, match.getTokens());
            OptionalDouble confidence = Token.mergeFields("confidence", Double.class, match.getTokens()).stream()
                    .mapToDouble(x -> x).average();
            Optional<String> task = Token.getAny("focus", String.class, match.getTokens());

            Token token = new Token()
                    .add("intent", "select_task")
                    .add("origin", origin)
                    .add("task", task.get());

            if(confidence.isPresent()) {
                token = token.add("confidence", confidence.getAsDouble());
            }

            return token;
        });


        //forward all input tokens that already have an intent
        InputNode intentNode= new InputNode(t -> t.has("intent"));
        fc.addFusionNode("intent_forward", intentNode, match -> {
            return match.getTokens().get(0);
        });
    }

    protected void createGrammarComponent() {
        // speech-recognition-service of the step-dp
        GrammarManager gm = MyGrammar.create();
        Grammar grammar = gm.createGrammar();
        SpeechRecognitionClient src = new SpeechRecognitionClient(app,"localhost", 9696, (token)-> {
            // TODO use resolution on token
            Optional<Map> semantic = token.get(Map.class, "semantic");
            if(!semantic.isPresent()) {
                return;
            }

            // TODO atm. intent is copied from the speech semantics
            Token processedToken = new Token((Map<String, Object>) semantic.get());
//            if(intent.isPresent()) {
//                processedToken = processedToken.add("intent", intent.get());
//            }
            // add token to fc
            FusionComponent fc = getComponent("fusion", FusionComponent.class).get();
            fc.addToken(processedToken);
        });
        String grammarStr = grammar.toString();
        src.setGrammar("main", grammarStr);
        src.initGrammar();
        src.init();
    }
}

