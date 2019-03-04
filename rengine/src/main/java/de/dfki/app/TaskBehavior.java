package de.dfki.app;

import de.dfki.dialog.*;
import de.dfki.rengine.RuleSystem;
import de.dfki.rengine.Token;
import de.dfki.sc.Parser;
import de.dfki.sc.SCEngine;
import de.dfki.sc.SCMain;
import de.dfki.sc.StateChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class TaskBehavior implements StateBehavior {
    private static final Logger log = LoggerFactory.getLogger(TaskBehavior.class);
    private RuleSystem rs;
    private TagSystem tagSystem;
    private StateHandler2 stateHandler;

    @Override
    public void init(Dialog dialog) {
        rs = dialog.getRuleSystem();
        tagSystem = dialog.getTagSystem();
        initTaskMode();
        MetaDialog.createGreetingsRule(dialog);

        URL resource = SCMain.class.getResource("/sc/simple.scxml");
        StateChart sc = null;
        try {
            sc = Parser.parse(resource);
            SCEngine engine = new SCEngine(sc);
            AtomicInteger counter = new AtomicInteger(0);
//            engine.addCondition("cond1", () -> counter.getAndIncrement() % 2 == 0);
            engine.addCondition("cond1", () -> true);
            engine.addOnEntry("outputTaskSummary", () -> outputTaskSummary());
            stateHandler = new StateHandler2(dialog, engine);

            Map<String, Set<String>> ruleActivation = Parser.loadActivations();
            for (var entry : ruleActivation.entrySet()) {
                String state = entry.getKey();
                for (String rule : entry.getValue()) {
                    tagSystem.addTag(rule, state);
                }
            }
            stateHandler.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void deinit() {
//        this.stateHandler.quit();
        //TODO impl
    }

    @Override
    public Object createSnapshot() {
        return stateHandler.createSnapshot();
    }

    @Override
    public void loadSnapshot(Object snapshot) {
        if (!(snapshot instanceof SCEngine.ObjState)) {
            throw new IllegalArgumentException("expected SCEngine.ObjState as type");
        }
        stateHandler.loadSnapshot((SCEngine.ObjState) snapshot);
    }

    public void outputTaskSummary() {
        rs.addToken(new Token("output_tts", "Okay: <summary over all tasks>"));
    }

    public void initTaskMode() {
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
                        stateHandler.fire("show_tasks");
                    });
        });
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
                        stateHandler.fire("hide_tasks");
                    });
        });
        rs.setPriority("hide_tasks", 20);


        /**
         */
        rs.addRule("select_task", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("select_task"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);

                        Optional<String> task = t.payload.getPayload("task")
                                .filter(s -> s instanceof String)
                                .map(s -> (String) s);


                        Optional<Double> confidence = t.payload.getPayload("confidence")
                                .filter(c -> c instanceof Double)
                                .map(c -> (Double) c);

                        //what should happen if the rules 'finishes'
                        Runnable execute = () -> {
                            String msg = String.format("There is a new urgent task '%s' : A UR-3 robot stopped functioning correctly", t.payload);
                            sys.addToken(new Token("output_tts", msg));
                            sys.addToken(new Token("output_image", "http://.../"));
                            createAcceptTaskRule(task.get());
                            stateHandler.fire("show_task");
                        };

                        if (task.isPresent()) {
                            if (confidence.isPresent() && confidence.get() < 0.3) {
                                System.out.println("Please confirm your selection for " + task.get());
                                MetaDialog.createConfirmRule(sys, "confirm_task", execute, () -> {
                                });
                                tagSystem.addTag("confirm_task", stateHandler.getCurrentState());
                            } else {
                                execute.run();
                            }
                        } else {
                            //TODO check for number of available tasks
                            sys.addToken(new Token("output_tts", "which task do you mean?"));
                            specifyTaskRule("select_task_supp");
                        }
                    });
        });
        rs.setPriority("select_task", 20);


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
                        stateHandler.fire("show_task");
                        sys.addToken(new Token("output_tts", "woah"));
                    });
        });
        rs.setPriority(rule, 20);
        tagSystem.addTag("select_task_supp", stateHandler.getCurrentState());
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

                        Optional<Double> confidence = t.payload.getPayload("confidence")
                                .filter(c -> c instanceof Double)
                                .map(c -> (Double) c);

                        if (confidence.isPresent() && confidence.get() < 0.3) {
                            String tts = String.format("Please confirm your selection for task '%s'", taskId);
                            sys.addToken(new Token("output_tts", tts));
                            sys.disable("accept_task");

                            MetaDialog.createConfirmRule(sys, "confirm_task",
                                    () -> {
                                        deinit();
                                        stateHandler.fire("task_accepted");
                                        String acceptTts = String.format("Okay, let's do task '%s'", taskId);
                                        sys.addToken(new Token("output_tts", acceptTts));
                                    }, () -> {
                                        createAcceptTaskRule(taskId);
                                        sys.addToken(new Token("output_tts", "Okay."));
                                    });
                            // associate the confirm_task rule to the current state.
                            tagSystem.addTag("confirm_task", stateHandler.getCurrentState());
                        } else {
                            stateHandler.fire("task_accepted");
                            String acceptTts = String.format("Okay, let's do task '%s'", taskId);
                            sys.addToken(new Token("output_tts", acceptTts));
                        }


                    });
        });
        rs.setPriority("accept_task", 20);
    }

    @Override
    public StateHandler2 getStateHandler() {
        return stateHandler;
    }
}
