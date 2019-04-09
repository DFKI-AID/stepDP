package de.dfki.step.dialog;

import de.dfki.step.core.ComponentManager;
import de.dfki.step.core.TagSystemComponent;
import de.dfki.step.core.TokenComponent;
import de.dfki.step.output.PresentationComponent;
import de.dfki.step.core.CoordinationComponent;
import de.dfki.step.core.Token;
import de.dfki.step.sc.SimpleStateBehavior;
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

    private TokenComponent tc;
    private CoordinationComponent rc;
    private PresentationComponent pc;
    private TagSystemComponent ts;
    private MetaFactory metaFactory;


    public TaskBehavior() throws URISyntaxException {
        super("/sc/task_behavior");
    }

    @Override
    public void init(ComponentManager cm) {
        super.init(cm);
        metaFactory = new MetaFactory(cm);
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

    @Override
    public Set<String> getActiveRules(String state) {
        if(Objects.equals(state, "Idle")) {
            return Set.of("show_tasks",  "proactive_idle");
        }
        if(Objects.equals(state, "Choice")) {
            return Set.of("select_task",  "hide_tasks");
        }
        if(Objects.equals(state, "Info")) {
            return Set.of("show_tasks",  "hide_tasks", "select_task", "accept_task");
        }
        if(Objects.equals(state, "Selected")) {
            return Set.of("show_navigation");
        }


        //log.warn("no rules for state {}", state);
        return Collections.EMPTY_SET;
    }

    public Boolean cond1() {
        return true;
    }

    public void outputTaskSummary() {
        cm.retrieveComponent(PresentationComponent.class)
                .present(PresentationComponent.simpleTTS("Okay: <summary over all tasks>"));
    }

    public void outputTaskInfo() {
        if (currentTask == null) {
            log.warn("Can't output task info: `currentTask` is not set");
            return;
        }

        //TODO load from db

        String msg = String.format("There is a new urgent task '%s' : A UR-3 robot stopped functioning correctly", currentTask);
        cm.retrieveComponent(PresentationComponent.class)
                .present(PresentationComponent.simpleTTS(msg));
    }

    public void hideTaskInfo() {
        cm.retrieveComponent(PresentationComponent.class)
                .present(PresentationComponent.simpleTTS("Okay: <hide menu in hololens>"));
        //TODO update state for hololens
        //or just use state chart
    }

    public void initTaskMode() {
        tc = cm.retrieveComponent(TokenComponent.class);
        rc = cm.retrieveComponent(CoordinationComponent.class);
        pc = cm.retrieveComponent(PresentationComponent.class);
        ts = cm.retrieveComponent(TagSystemComponent.class);

        /**
         * "Which tasks are available?"
         * Show the worker some tasks he can work on
         */
        rs.addRule("show_tasks", () -> tc.getTokens().stream()
                .filter(t -> t.payloadEquals("intent", "show_tasks"))
                .findFirst()
                .ifPresent(t -> {
                    rc.add("show_tasks", () -> {
                        stateHandler.fire("show_tasks");
                    }).attachOrigin(t);
                }));


        /**
         * "Hide the available task list in the hololens
         */
        rs.addRule("hide_tasks", () -> tc.getTokens().stream()
                .filter(t -> t.payloadEquals("intent", "hide_tasks"))
                .findFirst()
                .ifPresent(t -> {
                    rc.add("hide_tasks", () -> {
                        stateHandler.fire("hide_tasks");
                    });
                }));


        MetaFactory.timeoutRule(cm, "proactive_idle", Duration.ofSeconds(5l), () -> {
            Token output = PresentationComponent.simpleTTS("Hey! You can ask me to show available tasks if you are ready.");
            pc.present(output);
        });
        ts.addTag("proactive_idle", "Idle");


        metaFactory.selectRule("select_task", List.of("task1", "task2", "task3"), (task) -> {
            //TODO filter for available tasks here?
            this.currentTask = task;
            rs.removeRule("specify_select_task");
            rs.removeRule("confirm_select_task");
            stateHandler.fire("show_task");
        });
        //TODO tags and the removing of the functions should be done automatically?
        ts.addTag("specify_select_task", "Choice");
        ts.addTag("confirm_select_task", "Choice");
        ts.addTag("specify_select_task", "Info");
        ts.addTag("confirm_select_task", "Info");

        // convert 'show_navigation' intent to 'show_navigation' event
        rs.addRule("show_navigation", () -> {
            MetaFactory.filterIntent("request", tc.getTokens().stream())
                    .filter(t -> t.payloadEquals("object", "navigation"))
                    .forEach(t -> {
                        rc.add(() -> {
                            stateHandler.fire("show_navigation");
                        }).attachOrigin(t);
                    });
        });

        rs.addRule("provide_tool_info", () -> {
            MetaFactory.filterIntent("request", tc.getTokens().stream())
                    .filter(t -> t.payloadEquals("object", "tools"))
                    .forEach(t -> {
                        rc.add(() -> {
                            tts("you need the following tools...");
                        }).attachOrigin(t);
                    });
        });

        createAcceptTaskRule();


        rs.addRule("add_move_action", () -> {
            Optional<Token> token = tc.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "addAtomicAction"))
                    .filter(t -> t.payloadEquals("type", "move"))
                    .findFirst();

            if (!token.isPresent()) {
                return;
            }

            Token intent = token.get();
            if (!intent.get("object").isPresent()) {
                rc.add(() -> {
                    tts("Where do you want me to go?");
                    metaFactory.specifyRule("specify_add_move_action", (specification) -> {
                        String object = specification;
                        Optional<Object> action = intent.get("type");
                        rs.removeRule("specify_add_move_action");
                        // of Atomic Action
                    });

                }).attachOrigin(intent);
            }

            rc.add(() -> {
                String object = (String) intent.get("object").get();
                Optional<Object> action = intent.get("type");
                rs.removeRule("specify_add_move_action");
                // of Atomic Action
            }).attachOrigin(intent);
        });
        tagSystem.addTag("add_move_action", "CreateTask");
        rs.disable("add_move_action");

        Set<String> rules = tagSystem.getTagged("CreateTask");
        rules.forEach(r -> rs.enable(r));

    }

    protected void tts(String utterance) {
        var pc = getComponentManager().retrieveComponent(PresentationComponent.class);
        pc.present(PresentationComponent.simpleTTS(utterance));
    }


    /**
     * If the system can't derive the intended task referred by the user, the user
     * may specify his request by "the first task"
     */
    private void specifyTaskRule(String rule) {
        rs.addRule(rule, () -> {
            tc.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "select_task_supp"))
                    .findFirst()
                    .ifPresent(t -> {
                        Optional<Object> taskName = t.get("task");
                        if (!taskName.isPresent()) {
                            log.warn("no task info available. missing tag?");
                            return;
                        }

                        rc.add(() -> {
                            //TODO unchecked
                            String task = (String) taskName.get();
                            TaskBehavior.this.currentTask = task;
                            rs.removeRule(rule);
                            stateHandler.fire("show_task");
                        }).attachOrigin(t);
                    });
        });
        tagSystem.addTag("select_task_supp", stateHandler.getCurrentState());
    }

    /**
     *
     */
    private void createAcceptTaskRule() {
        rs.addRule("accept_task", () -> {
            tc.getTokens().stream()
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
                            rc.add(() -> {
                                String tts = String.format("Please confirm your selection for task '%s'.", currentTask);
                                this.tts(tts);
                                metaFactory.createInformAnswer("confirm_task",
                                        () -> {
                                            stateHandler.fire("task_accepted");
                                            tts(acceptTts);
                                        }, () -> {
                                            tts("Okay.");
                                        });
                                // associate the confirm_task rule to the current state.
                                tagSystem.addTag("confirm_task", stateHandler.getCurrentState());
                            }).attachOrigin(t);
                            return;
                        }

                        rc.add(() -> {
                            stateHandler.fire("task_accepted");
                            tts(acceptTts);
                        }).attachOrigin(t);

                    });
        });
    }
}
