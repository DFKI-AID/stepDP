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
    private final String taskSelectionTag = "TASK_SELECTION";
    private final AppGrammar appGrammar;


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
                        sys.enable("hide_tasks");
                        rs.disable("show_tasks");
                        sys.addToken(new Token("output_tts", "Okay: <summary over all tasks>"));
                    });
        });
        tagSystem.addTag(rs.getRule("show_tasks").get(), taskSelectionTag);
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
                        sys.enable("show_tasks");
                        rs.disable("hide_tasks");
                        rs.disable("accept_task");
                        sys.addToken(new Token("output_tts", "Okay: <hide menu in hololens>"));
                    });
        });
        tagSystem.addTag(rs.getRule("hide_tasks").get(), taskSelectionTag);
        rs.setPriority("hide_tasks", 20);


        /**
         * If the system can't derive the intended task referred by the user, the user
         * may specify his request by "the first task"
         */
        rs.addRule("select_task_supp", (sys) -> {
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
                        sys.addToken(new Token("show_task_info", taskName.get()));
                        rs.disable("select_task_supp");
                    });
        });
        rs.disable("select_task_supp");
        tagSystem.addTag(rs.getRule("select_task_supp").get(), taskSelectionTag);
        rs.setPriority("select_task_supp", 20);


        /**
         */
        rs.addRule("select_task", (sys) -> {
            Optional<String> visualFocus = sys.getTokens().stream()
                    .filter(t -> t.topicIs("visual_focus"))
                    .map(t -> (String) t.payload)
                    .findFirst();

            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("select_task"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        rs.disable("select_task_supp");
                        rs.disable("accept_task");

                        if (visualFocus.isPresent()) {
                            sys.addToken(new Token("show_task_info", visualFocus.get()));
                        } else {
                            //TODO check for number of available tasks
                            sys.addToken(new Token("output_tts", "which task do you mean?"));
                            rs.enable("select_task_supp");
                        }
                    });
        });
        tagSystem.addTag(rs.getRule("select_task").get(), taskSelectionTag);
        rs.setPriority("select_task", 20);


        rs.addRule("show_task_info", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("show_task_info"))
                    .findFirst()
                    .ifPresent(t -> {
//                        Optional<Object> task = t.payload.getPayload("task");
//                        if (!task.isPresent()) {
//                            log.warn("Ignoring 'accept_task' intent: missing slot 'task'. got: {}", t.payload);
//                            return;
//                        }

                        sys.removeToken(t);
                        String msg = String.format("There is a new urgent task '%s' : A UR-3 robot stopped functioning correctly", t.payload);
                        sys.addToken(new Token("output_tts", msg));
                        sys.addToken(new Token("output_image", "http://.../"));

                        createAcceptTaskRule((String) t.payload);
                    });

        });
        tagSystem.addTag(rs.getRule("show_task_info").get(), taskSelectionTag);
        rs.setPriority("show_task_info", 30);



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

                        createConfirmRule(sys, "confirm_task",
                                () -> {
                                    deinitTaskMode();
                                    String acceptTts = String.format("Okay, let's do task '%s'", taskId);
                                    sys.addToken(new Token("output_tts", acceptTts));
                                }, () -> {
                                    createAcceptTaskRule(taskId);
                                    sys.addToken(new Token("output_tts", "Okay."));
                                });
                    });
        });
//        rs.disable("accept_task");
        tagSystem.addTag(rs.getRule("accept_task").get(), taskSelectionTag);
        rs.setPriority("accept_task", 20);
    }

    public void deinitTaskMode() {
        tagSystem.getTagged(taskSelectionTag).forEach(r ->
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


    public RuleSystem getRuleSystem() {
        return rs;
    }

    public TagSystem<Rule> getTagSystem() {
        return tagSystem;
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
        createInterruptRule(rs);

        rs.addRule("manual_intent", (sys) -> {
            Intent intent = intentQueue.poll();
            if (intent == null) {
                return;
            }
            sys.addToken(new Token("intent", intent));
        });
        rs.setPriority("manual_intent", 10);

        //go back rule:


        //simulates visual focus
        rs.addRule("visual_focus", (sys) -> {
            if ((new Random().nextBoolean())) {
                sys.addToken(new Token("visual_focus", "task" + (new Random()).nextInt(3)));
            }
        });
        rs.setPriority("visual_focus", 10);
        tagSystem.addTag(rs.getRule("visual_focus").get(), "simulation");


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


        rs.addRule("InterruptTTS", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("interrupt_tts"))
                    .findFirst()
                    .ifPresent(t -> {
                        System.out.println("-stopping tts-");
                    });
        });
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
