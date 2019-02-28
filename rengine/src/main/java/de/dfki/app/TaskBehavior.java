package de.dfki.app;

import de.dfki.dialog.*;
import de.dfki.rengine.RuleSystem;
import de.dfki.rengine.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 *
 */
public class TaskBehavior implements Behavior {
    private static final Logger log = LoggerFactory.getLogger(TaskBehavior.class);
    private final String tag = "TaskAssignment";
    private final String tagIdle = tag + ".Idle";
    private final String tagChoice = tag + ".Choice";
    private final String tagInfo = tag + ".Info";
    private StateHandler2 stateHandler;
    private RuleSystem rs;
    private TagSystem tagSystem;



    @Override
    public void init(Dialog dialog) {
        this.stateHandler = new StateHandler2(dialog, tagIdle, tagChoice, tagInfo);
        rs = dialog.getRuleSystem();
        tagSystem = dialog.getTagSystem();
        initTaskMode();
    }

    @Override
    public void deinit() {
        this.stateHandler.quit();
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

                        //TODO only create accept rule on low confidence?
                        MetaDialog.createConfirmRule(sys, "confirm_task",
                                () -> {
                                    deinit();
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
}
