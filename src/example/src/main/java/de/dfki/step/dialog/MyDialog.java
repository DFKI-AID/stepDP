package de.dfki.step.dialog;


import de.dfki.step.rengine.Token;
import de.dfki.step.srgs.Grammar;
import de.dfki.step.srgs.GrammarManager;
import de.dfki.step.srgs.MyGrammar;
import de.dfki.step.web.SpeechRecognitionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;


/**
 *
 */
public class MyDialog extends Dialog {
    private static final Logger log = LoggerFactory.getLogger(MyDialog.class);
    private MyGrammar appGrammar;

    public MyDialog() {
        try {
            TaskBehavior taskBehavior = new TaskBehavior();
            this.addBehavior("task_behavior", taskBehavior);

        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not load task behavior", e);
        }

        MetaFactory.createUndoRule(this);

        MetaFactory.turnGrabRule(this, "interrupt", (token) -> {
            System.out.println("TODO interrupt output + \"yes?\"");
        });


        MetaFactory.createGreetingsRule(this);

        TimeBehavior timeBehavior = new TimeBehavior();
        this.addBehavior("time_behavior", timeBehavior);


        MetaFactory.createRepeatRule(this, "request_repeat_tts", "I did not say anything.");
        MetaFactory.snapshotRule(this);
    }

    @Override
    public void init() {
        super.init();

        // we use the speech-recogntion-service of the step-dp
        GrammarManager gm = MyGrammar.create();
        Grammar grammar = gm.createGrammar();
        SpeechRecognitionClient src = new SpeechRecognitionClient("localhost", 9696, (token)-> {
            // resolve token
            Token processedToken;
            // add token to fc
            //TODO feed into input
            //fc.addToken(processedToken);
        });
        String grammarStr = grammar.toString();
        src.setGrammar("main", grammarStr);
        src.initGrammar();
    }

    @Override
    public void update() {
        super.update();
    }
}

