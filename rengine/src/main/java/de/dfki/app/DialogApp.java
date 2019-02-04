package de.dfki.app;


import de.dfki.rengine.*;
import de.dfki.rengine.grammar.GrammarManager;
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

        rs.addRule("task_info_supp", (sys) -> {
            final Pattern pattern = Pattern.compile("[the ]?(first|second|third) task");
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("asr"))
                    .map(t -> Tuple.of(t, match(pattern, (String) t.payload)))
                    .filter(tuple -> !tuple.second.isEmpty())
//                    .map(g -> g.get(0))
//                    .filter(second -> Objects.equals(second.payload, "the first task"))
                    .findFirst()
                    .ifPresent(tuple -> {
                        String taskName = tuple.second.get(0);
                        sys.removeToken(tuple.first);
                        sys.addToken(new Token("show_task_info", taskName));
                        rs.block("task_info_supp");
                    });
        });
        rs.block("task_info_supp");
        tagSystem.addTag(rs.getRule("task_info_supp").get(), taskSelectionTag);
        rs.setPriority("task_info_supp", 20);


        rs.addRule("task_info", (sys) -> {
            Optional<String> visualFocus = sys.getTokens().stream()
                    .filter(t -> t.topicIs("visual_focus"))
                    .map(t -> (String) t.payload)
                    .findFirst();

            sys.getTokens().stream()
                    .filter(t -> t.topicIs("asr"))
                    .filter(t -> Objects.equals(t.payload, "can you give me more information on this task"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);

                        if (visualFocus.isPresent()) {
                            sys.addToken(new Token("show_task_info", visualFocus.get()));
                        } else {
                            //TODO check for number of available tasks
                            sys.addToken(new Token("output_tts", "which task do you mean?"));
                            rs.block("accept_task");
                            rs.enable("task_info_supp");
                        }
                    });
        });
        tagSystem.addTag(rs.getRule("task_info").get(), taskSelectionTag);
        rs.setPriority("task_info", 20);


        rs.addRule("show_task_info", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("show_task_info"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        String msg = String.format("There is a new urgent task '%s' : A UR-3 robot stopped functioning correctly", t.payload);
                        sys.addToken(new Token("output_tts", msg));
                        sys.addToken(new Token("output_image", "http://.../"));
                        rs.enable("accept_task");
                    });

        });
        tagSystem.addTag(rs.getRule("show_task_info").get(), taskSelectionTag);
        rs.setPriority("show_task_info", 30);


        rs.addRule("accept_task", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("asr"))
                    .filter(t -> Objects.equals(t.payload, "accept this"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.addToken(new Token("output_tts", "Please confirm your selection for task"));
//                        sys.disable("accept_task");

                        // finish task mode by disabling all corresponding rules
//                        tagSystem.getTagged(taskSelectionTag).stream()
//                                .forEach(rule -> sys.block(rule));

                        createConfirmRule(sys, "confirm",
                                () -> {
                                    deinitTaskMode();
                                    sys.addToken(new Token("output_tts", "Okay, let's do task x"));
                                }, () -> {
                                    sys.addToken(new Token("output_tts", "Okay."));
                                });
//                        deinitTaskMode();
                    });
        });
        rs.block("accept_task");
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


    private Queue<String> speechInputQueue = new ConcurrentLinkedDeque<>();

    public void addAsr(String text) {
        log.info("Received ASR: '{}'", text);
        speechInputQueue.add(text);
    }


    @Override
    public void init() {

        Thread asrSimulationThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Scanner scanner = new Scanner(System.in);
                String text = scanner.nextLine();
                speechInputQueue.add(text);
            }
        });
        asrSimulationThread.setDaemon(true);
//        asrSimulationThread.start();


        initTaskMode();

        createUndoRule(rs);

        rs.addRule("asr", (sys) -> {
            String text = speechInputQueue.poll();
            if (text == null) {
                return;
            }
            sys.addToken(new Token("asr", text));
        });
        rs.setPriority("asr", 10);

        //go back rule:


        //simulates visual focus
        rs.addRule("visual_focus", (sys) -> {
            if ((new Random().nextBoolean())) {
                sys.addToken(new Token("visual_focus", "task" + (new Random()).nextInt(3)));
            }
        });
        rs.setPriority("visual_focus", 10);
        tagSystem.addTag(rs.getRule("visual_focus").get(), "simulation");


        rs.addRule("got_it", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("asr"))
                    .filter(t -> Objects.equals(t.payload, "ok, i got it")) //TODO match with grammar
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.addToken(new Token("interrupt_tts", null));
                    });
        });
        rs.setPriority("got_it", 20);



        rs.addRule("greetings", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("asr"))
                    .filter(t -> Objects.equals(t.payload, "hi")) //TODO match with grammar
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.addToken(new Token("greetings", null));
                    });
        });
        rs.setPriority("greetings", 20);


        rs.addRule("hello", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("greetings"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.addToken(new Token("output_tts", "hello!"));
                        sys.block("hello", Duration.ofSeconds(4));
                    });
        });
        rs.setPriority("hello", 30);

        rs.addRule("request_time", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("asr"))
                    .filter(t -> Objects.equals(t.payload, "what time is it"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        var now = LocalDateTime.now();
                        var tts = "it is " + now.getHour() + ":" + now.getMinute(); //TODO improve
                        sys.addToken(new Token("output_tts", tts));
                        sys.block("request_time", Duration.ofMillis(3000));
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

    @Override
    public void deinit() {

    }




}
