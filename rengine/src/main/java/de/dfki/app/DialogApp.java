package de.dfki.app;


import de.dfki.rengine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class DialogApp extends Dialog {
    private static final Logger log = LoggerFactory.getLogger(DialogApp.class);
    private final String tag = "TaskAssignment";
    private final String tagIdle = tag + ".Idle";
    private final String tagChoice = tag + ".Choice";
    private final String tagInfo = tag + ".Info";
    protected final StateHandler2 stateHandler = new StateHandler2(this, tagIdle, tagChoice, tagInfo);

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


    public void initTaskMode() {
        var rs = getRuleSystem();
        var tagSystem = getTagSystem();





        /**
         * "Which tasks are available?"
         * Show the worker some tasks he can work on
         */
        rs.addRule("show_tasks", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("show_tasks"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.addToken(new Token("output_tts", "Okay: <summary over all tasks>"));
                        stateHandler.enter(tagChoice);
                    });
        });
        tagSystem.addTag("show_tasks", tag);
        tagSystem.addTag("show_tasks", tagIdle);
        rs.setPriority("show_tasks", 20);


        /**
         * "Hide the available task list in the hololens
         */
        rs.addRule("hide_tasks", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("hide_tasks"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.addToken(new Token("output_tts", "Okay: <hide menu in hololens>"));
                        stateHandler.enter(tagIdle);
                    });
        });
        tagSystem.addTag("hide_tasks", tag);
        tagSystem.addTag("hide_tasks", tagChoice);
        tagSystem.addTag("hide_tasks", tagInfo);
        rs.setPriority("hide_tasks", 20);


        /**
         */
        rs.addRule("select_task", (sys) -> {
            Optional<String> focus = sys.getTokens().stream()
                    .filter(t -> t.topicIs("focus"))
                    .map(t -> (String) t.payload)
                    .findFirst();

            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("select_task"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);

                        if (focus.isPresent()) {
                            String msg = String.format("There is a new urgent task '%s' : A UR-3 robot stopped functioning correctly", t.payload);
                            sys.addToken(new Token("output_tts", msg));
                            sys.addToken(new Token("output_image", "http://.../"));
                            createAcceptTaskRule(focus.get());
                            stateHandler.enter(tagInfo);
                        } else {
                            //TODO check for number of available tasks
                            sys.addToken(new Token("output_tts", "which task do you mean?"));
                            specifyTaskRule("select_task_supp");
                        }
                    });
        });
        tagSystem.addTag("select_task", tag);
        tagSystem.addTag("select_task", tagChoice);
        tagSystem.addTag("select_task", tagInfo);
        rs.setPriority("select_task", 20);


        this.stateHandler.enter(tagIdle);
    }


    /**
     * If the system can't derive the intended task referred by the user, the user
     * may specify his request by "the first task"
     */
    private void specifyTaskRule(String rule) {
        rs.addRule(rule, (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("select_task_supp"))
                    .findFirst()
                    .ifPresent(t -> {
                        Optional<Object> taskName = t.payload.getPayload("task");
                        if (!taskName.isPresent()) {
                            log.warn("no task info available. missing tag?");
                            return;
                        }
                        sys.removeToken(t);
                        rs.removeRule(rule);
                        createAcceptTaskRule((String) taskName.get());
                        stateHandler.enter(tagInfo);
                        sys.addToken(new Token("output_tts", "woah"));
                    });
        });
        tagSystem.addTag(rule, tag);
        tagSystem.addTag(rule, tagChoice);
        rs.setPriority(rule, 20);
    }

    private void createAcceptTaskRule(String taskId) {
        rs.addRule("accept_task", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("accept_task"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        String tts = String.format("Please confirm your selection for task '%s'", taskId);
                        sys.addToken(new Token("output_tts", tts));
                        sys.disable("accept_task");

                        //TODO only create accept on low confidence?
                        createConfirmRule(sys, "confirm_task",
                                () -> {
                                    deinitTaskMode();
                                    String acceptTts = String.format("Okay, let's do task '%s'", taskId);
                                    sys.addToken(new Token("output_tts", acceptTts));
                                }, () -> {
                                    createAcceptTaskRule(taskId);
                                    sys.addToken(new Token("output_tts", "Okay."));
                                });
                        tagSystem.addTag("confirm_task", tag);
                        tagSystem.addTag("confirm_task", tagInfo);
                    });
        });
//        rs.disable("accept_task");
        tagSystem.addTag("accept_task", tag);
        tagSystem.addTag("accept_task", tagInfo);
        rs.setPriority("accept_task", 20);
    }

    public void deinitTaskMode() {
        tagSystem.getTagged(tag).forEach(r ->
                rs.removeRule(r)
        );
    }

    private final String navTag = "Navigation";

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


    private Queue<Intent> intentQueue = new ConcurrentLinkedDeque<>();


    public void addIntent(Intent intent) {
        log.info("Received intent: {}", intent);
        intentQueue.add(intent);
    }

    @Override
    public void init() {
        initTaskMode();

        createUndoRule(rs);
        tagSystem.addTag("undo", "meta");
        tagSystem.addTag("update_undo", "meta");
        createInterruptRule(rs);
        tagSystem.addTag("interrupt", "meta");
        tagSystem.addTag("request_repeat_tts", "meta");

        rs.addRule("manual_intent", (sys) -> {
            Intent intent = intentQueue.poll();
            if (intent == null) {
                return;
            }
            sys.addToken(new Token("intent", intent));
        });
        tagSystem.addTag("manual_intent", "simulation");
        rs.setPriority("manual_intent", 10);

        //go back rule:


        //simulates visual focus
        rs.addRule("focus", (sys) -> {
//            if ((new Random().nextBoolean())) {
//                sys.addToken(new Token("focus", "task" + (new Random()).nextInt(3)));
//            }
        });
        rs.setPriority("focus", 10);
        tagSystem.addTag("focus", "simulation");


        rs.addRule("greetings", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("greetings"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.addToken(new Token("output_tts", "hello!"));
                        sys.disable("greetings", Duration.ofSeconds(4));
                    });
        });
        rs.setPriority("greetings", 20);
        tagSystem.addTag("greetings", "meta");


        rs.addRule("request_time", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("time_request"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        var now = LocalDateTime.now();
                        var tts = "it is " + now.getHour() + ":" + now.getMinute(); //TODO improve
                        sys.addToken(new Token("output_tts", tts));
                        sys.disable("request_time", Duration.ofMillis(3000));
                    });
        });
        tagSystem.addTag("request_time", "meta");


        createRepeatRule(rs, "request_repeat_tts", "I did not say anything.");


        rs.addRule("TTS", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("output_tts"))
                    .findFirst()
                    .ifPresent(t -> {
                        System.out.println("System: " + t.payload);
                        sys.removeRule("request_repeat_tts");
                        createRepeatRule(sys, "request_repeat_tts", (String) t.payload);
                        sys.setPriority("request_repeat_tts", 20);
                    });
            //TODO could als merge all TTS request into one


        });
        rs.setPriority("TTS", 100);
        tagSystem.addTag("TTS", "meta");

        rs.addRule("InterruptTTS", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("interrupt_tts"))
                    .findFirst()
                    .ifPresent(t -> {
                        System.out.println("-stopping tts-");
                    });
        });
        tagSystem.addTag("InterruptTTS", "meta");
    }

    @Override
    public void update() {
        hololensClient.updateGrammar(grammarManager);
    }

    private static void createInterruptRule(RuleSystem rs) {
        rs.addRule("interrupt", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("interrupt"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.addToken(new Token("interrupt_tts", null));
                    });
        });
        rs.setPriority("interrupt", 20);
    }

    @Override
    public void deinit() {

    }


}
