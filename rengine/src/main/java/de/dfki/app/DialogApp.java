package de.dfki.app;


import de.dfki.dialog.*;
import de.dfki.rengine.RuleSystem;
import de.dfki.rengine.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class DialogApp extends Dialog {
    private static final Logger log = LoggerFactory.getLogger(DialogApp.class);
    private final String navTag = "Navigation";

    private final AppGrammar appGrammar;
    private final HololensClient hololensClient = new HololensClient("10.2.0.32", 11000);

    public DialogApp() {
        this.appGrammar = new AppGrammar(this.grammarManager);
    }

    public static List<String> match(Pattern pattern, String s) {
        Matcher matcher = pattern.matcher(s);
        if (!matcher.find()) {
            return Collections.EMPTY_LIST;
        }

        List<String> groups = new ArrayList<>();
        for (int i = 0; i < matcher.groupCount(); i++) {
            groups.add(matcher.group(i));
        }
        return groups;
    }


    public void initNavMode() {
        //[1] where do I have go?
        // what do i have to do?

        //[2] which tools do I need?

        //pick up safety shoes!

        //[3] timeout... "go on; you are almost there"

        //[4] "what is this?" maybe meta-rule
    }

    public void deinitNavMode() {
        tagSystem.getTagged(navTag).forEach(r ->
                rs.removeRule(r)
        );
    }

    private final String solutionTag = "Solution";


    public void initSolutionMode() {
        // re-try
        // go to the next approach
        // go to the previous approach
    }

    public void deinitSolutionMode() {
        tagSystem.getTagged(solutionTag).forEach(r ->
                rs.removeRule(r)
        );
    }


    private Queue<Token> intentQueue = new ConcurrentLinkedDeque<>();


    public void addIntent(Token intent) {
        log.info("Received intent: {}", intent);
        intentQueue.add(intent);
    }

    @Override
    public void init() {
        TaskBehavior taskBehavior = new TaskBehavior();
        this.addBehavior("task_behavior", taskBehavior);
        taskBehavior.init(this);

        MetaFactory.createUndoRule(this);

        MetaFactory.turnGrabRule(this, "interrupt", (token) -> {
            System.out.println("TODO interrupt output + \"yes?\"");
        });

        tagSystem.addTag("request_repeat_tts", "meta");

        rs.addRule("manual_intent", () -> {
            Token intentToken = intentQueue.poll();
            if (intentToken == null) {
                return;
            }
            addToken(intentToken);
        });
        tagSystem.addTag("manual_intent", "simulation");

        //go back rule:


        //simulates visual focus
//        rs.add("focus", (sys) -> {
//            if ((new Random().nextBoolean())) {
//                sys.addToken(new Token("focus", "task" + (new Random()).nextInt(3)));
//            }
//        });
//        rs.setPriority("focus", 10);
//        tagSystem.addTag("focus", "simulation");


        TimeBehavior timeBehavior = new TimeBehavior();
        timeBehavior.init(this);


        MetaFactory.createRepeatRule(this, "request_repeat_tts", "I did not say anything.");

        MetaFactory.snapshotRule(this);

//        rs.add("TTS", (sys) -> {
//            sys.getTokens().stream()
//                    .filter(t -> t.topicIs("output_tts"))
//                    .forEach(t -> {
//                        if (!t.get("utterance").isPresent()) {
//                            log.warn("Missing utterance in 'output_tts' token. got {}", t);
//                            return;
//                        }
//
//                        String utterance = t.get("utterance").toString();
//                        System.out.println("System: " + utterance);
//                        sys.removeRule("request_repeat_tts");
//                        MetaFactory.createRepeatRule(sys, "request_repeat_tts", utterance);
//                        sys.setPriority("request_repeat_tts", 20);
//                    });
//            //TODO could als merge all TTS request into one
//        });
//        rs.setPriority("TTS", 100);
//        tagSystem.addTag("TTS", "meta");
    }

    @Override
    public void update() {
        hololensClient.updateGrammar(grammarManager);
    }


    @Override
    public void deinit() {

    }


}
