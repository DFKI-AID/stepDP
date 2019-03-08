package de.dfki.app;

import de.dfki.dialog.*;
import de.dfki.rengine.RuleSystem;
import de.dfki.rengine.Token;
import de.dfki.sc.Parser;
import de.dfki.sc.SCEngine;
import de.dfki.sc.SCMain;
import de.dfki.sc.StateChart;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class TaskBehavior implements StateBehavior {
    private static final Logger log = LoggerFactory.getLogger(TaskBehavior.class);
    private Dialog dialog;
    private RuleSystem rs;
    private TagSystem<String> tagSystem;
    private StateHandler2 stateHandler;
    private String currentTask;

    @Override
    public void init(Dialog dialog) {
        this.dialog = dialog;
        rs = dialog.getRuleSystem();
        tagSystem = dialog.getTagSystem();
        initTaskMode();
        MetaFactory.createGreetingsRule(dialog);

        URL resource = SCMain.class.getResource("/sc/simple.scxml");
        StateChart sc = null;
        try {
            sc = Parser.parse(resource);
            SCEngine engine = new SCEngine(sc);
            AtomicInteger counter = new AtomicInteger(0);
//            engine.addCondition("cond1", () -> counter.getAndIncrement() % 2 == 0);
//            engine.addCondition("cond1", () -> true);
//            engine.addFunction("outputTaskSummary", () -> outputTaskSummary());
//            engine.addFunction("outputTaskInfo", () -> outputTaskInfo());
            engine.addFunctions(this);
            engine.addConditions(this);
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
    public Map<String, Object> createSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("sh", stateHandler.createSnapshot());
        snapshot.put("current_task", this.currentTask);
        return snapshot;
    }

    @Override
    public void loadSnapshot(Object snapshot) {
        if (!(snapshot instanceof Map)) {
            throw new IllegalArgumentException("expected Map as type");
        }

        Map<String, Object> snapshotMap = (Map<String, Object>) snapshot;
        Object sh = snapshotMap.get("sh");
        if (!(sh instanceof SCEngine.ObjState)) {
            throw new IllegalArgumentException("expected SCEngine.ObjState as type for StateHandler");
        }
        stateHandler.loadSnapshot((SCEngine.ObjState) sh);

        Object currentTask = snapshotMap.get("current_task");
        if (currentTask != null) {
            if (!(currentTask instanceof String)) {
                throw new IllegalArgumentException("expected String as type for currentTask");
            }
        }
        this.currentTask = (String) currentTask;
    }

    public Boolean cond1() {
        return true;
    }

    public void outputTaskSummary() {
        dialog.present(new PresentationRequest("Okay: <summary over all tasks>"));
    }

    public void outputTaskInfo() {
        if (currentTask == null) {
            log.warn("Can't output task info: `currentTask` is not set");
            return;
        }

        //TODO load from db
        String msg = String.format("There is a new urgent task '%s' : A UR-3 robot stopped functioning correctly", currentTask);
        dialog.present(new PresentationRequest(msg));
        dialog.present(new PresentationRequest("http://.../"));
    }

    public void hideTaskInfo() {
        dialog.present(new PresentationRequest("Okay: <hide menu in hololens>"));
        //TODO update state for hololens
        //or just use state chart
    }

    public void initTaskMode() {
        /**
         * "Which tasks are available?"
         * Show the worker some tasks he can work on
         */
        rs.addRule("show_tasks", () -> dialog.getTokens().stream()
                .filter(t -> t.payloadEquals("intent", "show_tasks"))
                .findFirst()
                .ifPresent(t -> {
                    dialog.getRuleCoordinator().add("show_tasks", () -> {
                        stateHandler.fire("show_tasks");
                    }).attach("consumes", t);
                }));
        rs.setPriority("show_tasks", 20);


        /**
         * "Hide the available task list in the hololens
         */
        rs.addRule("hide_tasks", () -> dialog.getTokens().stream()
                .filter(t -> t.payloadEquals("intent", "hide_tasks"))
                .findFirst()
                .ifPresent(t -> {
                    dialog.getRuleCoordinator().add("hide_tasks", () -> {
                        stateHandler.fire("hide_tasks");
                    });
                }));
        rs.setPriority("hide_tasks", 20);


        /**
         */
//        rs.add("select_task", (sys) -> {
//            sys.getTokens().stream()
//                    .filter(t -> t.payloadEquals("intent", "select_task"))
//                    .findFirst()
//                    .ifPresent(t -> {
//                        sys.removeToken(t);
//
//                        Optional<String> task = t.get("task")
//                                .filter(s -> s instanceof String)
//                                .map(s -> (String) s);
//
//
//                        Optional<Double> confidence = t.get("confidence")
//                                .filter(c -> c instanceof Double)
//                                .map(c -> (Double) c);
//
//                        //what should happen if the functions 'finishes'
//                        Runnable execute = () -> {
//                            stateHandler.fire("show_task");
//                        };
//
//                        if (task.isPresent()) {
//                            TaskBehavior.this.currentTask = task.get();
//                            if (confidence.isPresent() && confidence.get() < 0.3) {
//                                System.out.println("Please confirm your selection for " + task.get());
//                                MetaFactory.createInformAnswer(sys, "confirm_task", execute, () -> {
//                                });
//                                tagSystem.addTag("confirm_task", stateHandler.getCurrentState());
//                            } else {
//                                execute.run();
//                            }
//                        } else {
//                            //TODO check for number of available tasks
//                            sys.addToken(new Token("output_tts").add("utterance", "which task do you mean?"));
//                            specifyTaskRule("select_task_supp");
//                        }
//                    });
//        });


        MetaFactory.selectRule(dialog, "select_task", List.of("task1", "task2", "task3"), (task) -> {
            //TODO filter for available tasks here?
            this.currentTask = task;
            rs.removeRule("specify_select_task");
            rs.removeRule("confirm_select_task");
            stateHandler.fire("show_task");
        });
        rs.setPriority("select_task", 20);
        //TODO tags and the removing of the functions should be done automatically
        dialog.getTagSystem().addTag("specify_select_task", "Choice");
        dialog.getTagSystem().addTag("confirm_select_task", "Choice");
        dialog.getTagSystem().addTag("specify_select_task", "Info");
        dialog.getTagSystem().addTag("confirm_select_task", "Info");

        // convert 'show_navigation' intent to 'show_navigation' event
        rs.addRule("show_navigation", () -> {
            MetaFactory.filterIntent("request", dialog.getTokens().stream())
                    .filter(t -> t.payloadEquals("object", "navigation"))
                    .forEach(t -> {
                        dialog.getRuleCoordinator().add(() -> {
                            stateHandler.fire("show_navigation");
                        }).attach("consumes", t);
                    });
        });

        rs.addRule("provide_tool_info", () -> {
            MetaFactory.filterIntent("request", dialog.getTokens().stream())
                    .filter(t -> t.payloadEquals("object", "tools"))
                    .forEach(t -> {
                        dialog.getRuleCoordinator().add(() -> {
                            dialog.present(new PresentationRequest("you need the following tools..."));
                        }).attach("consumes", t);
                    });
        });

        createAcceptTaskRule();




        rs.addRule("add_move_action", () -> {
            Optional<Token> token = dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "addAtomicAction"))
                    .filter(t -> t.payloadEquals("type", "move"))
                    .findFirst();

            if(!token.isPresent()) {
                return;
            }

            Token intent = token.get();
            if(!intent.get("object").isPresent()) {
                dialog.getRuleCoordinator().add(() -> {
                    dialog.present(new PresentationRequest("Where do you want me to go?"));
                    MetaFactory.specifyRule(dialog, "specify_add_move_action", (specification) -> {
                        String object = specification;
                        Optional<Object> action = intent.get("type");
                        rs.removeRule("specify_add_move_action");
                        // create Atomic Action
                    });

                }).attach("consumes", intent);
            }

            dialog.getRuleCoordinator().add(() -> {
                String object = (String) intent.get("object").get();
                Optional<Object> action = intent.get("type");
                rs.removeRule("specify_add_move_action");
                // create Atomic Action
            }).attach("consumes", intent);
        });
        tagSystem.addTag("add_move_action", "CreateTask");
        rs.disable("add_move_action");

        Set<String> rules = tagSystem.getTagged("CreateTask");
        rules.forEach(r -> rs.enable(r));

    }


    /**
     * If the system can't derive the intended task referred by the user, the user
     * may specify his request by "the first task"
     */
    private void specifyTaskRule(String rule) {
        rs.addRule(rule, () -> {
            dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "select_task_supp"))
                    .findFirst()
                    .ifPresent(t -> {
                        Optional<Object> taskName = t.get("task");
                        if (!taskName.isPresent()) {
                            log.warn("no task info available. missing tag?");
                            return;
                        }

                        dialog.getRuleCoordinator().add(() -> {
                            //TODO unchecked
                            String task = (String) taskName.get();
                            TaskBehavior.this.currentTask = task;
                            rs.removeRule(rule);
                            stateHandler.fire("show_task");
                        }).attach("consumes", t);
                    });
        });
        rs.setPriority(rule, 20);
        tagSystem.addTag("select_task_supp", stateHandler.getCurrentState());
    }

    /**
     *
     */
    private void createAcceptTaskRule() {
        rs.addRule("accept_task", () -> {
            dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "accept_task"))
                    .findFirst()
                    .ifPresent(t -> {

                        Optional<Double> confidence = t.get("confidence")
                                .filter(c -> c instanceof Double)
                                .map(c -> (Double) c);

                        if (currentTask == null) {
                            return;
                        }

                        String acceptTts = String.format("Okay, let's do task '%s'", currentTask);

                        if (confidence.isPresent() && confidence.get() < 0.3) {
                            dialog.getRuleCoordinator().add(() -> {
                                String tts = String.format("Please confirm your selection for task '%s'.", currentTask);
                                dialog.present(new PresentationRequest(tts));
                                MetaFactory.createInformAnswer(dialog, "confirm_task",
                                        () -> {
                                            stateHandler.fire("task_accepted");
                                            dialog.present(new PresentationRequest(acceptTts));
                                        }, () -> {
                                            dialog.present(new PresentationRequest("Okay."));
                                        });
                                // associate the confirm_task rule to the current state.
                                tagSystem.addTag("confirm_task", stateHandler.getCurrentState());
                            }).attach("consumes", t);
                            return;
                        }

                        dialog.getRuleCoordinator().add(() -> {
                            stateHandler.fire("task_accepted");
                            dialog.present(new PresentationRequest(acceptTts));
                        }).attach("consumes", t);

                    });
        });
        rs.setPriority("accept_task", 20);
    }

    @Override
    public StateHandler2 getStateHandler() {
        return stateHandler;
    }
}
