package de.dfki.step.dialog;


import de.dfki.step.srgs.MyGrammar;
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
    public void update() {
        super.update();
    }
}
