package de.dfki.step.dialog;

import de.dfki.step.rengine.Token;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;

/**
 *
 */
public class TaskBehavior extends SimpleStateBehavior {
    private static final Logger log = LoggerFactory.getLogger(TaskBehavior.class);

    private String currentTask;

    public TaskBehavior() throws URISyntaxException {
        super("/sc/task_behavior");
    }

    @Override
    public void init(Dialog dialog) {
        super.init(dialog);
        initTaskMode();
    }

    @Override
    public void deinit() {
        super.deinit();
    }

    @Override
    public PMap<String, Object> createSnapshot() {
        PMap<String, Object> snapshot = super.createSnapshot();
        snapshot = snapshot.plus("current_task", this.currentTask);
        return snapshot;
    }

    @Override
    public void loadSnapshot(Object snapshot) {
        var snapshotMap = (Map<String, Object>) snapshot;
        super.loadSnapshot(snapshot);
        try {
            Object currentTask = snapshotMap.get("current_task");
            this.currentTask = (String) currentTask;
        } catch (Exception ex) {
            throw new RuntimeException("could not reload snapshot", ex);
        }
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
//        dialog.present(new PresentationRequest("http://.../"));
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


        MetaFactory.timeoutRule(dialog, "proactive_idle", Duration.ofSeconds(5l), () -> {
            dialog.present(new PresentationRequest("Hey! You can ask me to show available tasks if you are ready."));
        });
        dialog.getTagSystem().addTag("proactive_idle", "Idle");


        MetaFactory.selectRule(dialog, "select_task", List.of("task1", "task2", "task3"), (task) -> {
            //TODO filter for available tasks here?
            this.currentTask = task;
            rs.removeRule("specify_select_task");
            rs.removeRule("confirm_select_task");
            stateHandler.fire("show_task");
        });
        //TODO tags and the removing of the functions should be done automatically?
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

            if (!token.isPresent()) {
                return;
            }

            Token intent = token.get();
            if (!intent.get("object").isPresent()) {
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
    }
}