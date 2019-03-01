package de.dfki.dialog;

import de.dfki.rengine.RuleSystem;
import de.dfki.rengine.Token;

import java.time.Duration;
import java.util.Objects;

/**
 *
 */
public class MetaDialog {
    public static void createGreetingsRule(Dialog dialog) {
        var rs = dialog.getRuleSystem();
        var tagSystem = dialog.getTagSystem();

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
    }

    public static void createConfirmRule(RuleSystem ruleSystem, String ruleName, Runnable yes, Runnable no) {
        ruleSystem.addRule(ruleName, (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> Objects.equals(t.payload.getIntent(), "accept")
                            || Objects.equals(t.payload.getIntent(), "reject"))
                    .findFirst()
                    .ifPresent(t -> {
                        if (Objects.equals(t.payload.getIntent(), "accept")) {
                            yes.run();
                        } else {
                            no.run();
                        }
                        sys.removeToken(t);
                        sys.removeRule(ruleName);
                    });
        });
        ruleSystem.setVolatile(ruleName, true);
        ruleSystem.setPriority(ruleName, 25);

        //TODO to avoid a deadlock:
        //TODO [1] it should be checked if there is still a confirmation active -> cancel it
        //TODO [2] use a different rule name and keep both confirmation rules active (only one rule will consume the response)
    }

    /**
     * Helps the user to understand the current context and what can be done.
     * E.g. show example sentences from the SRGS grammar, or generate them from the grammar
     *
     * @param rs
     */
    public static void createHelpRule(RuleSystem rs) {
        //TODO impl
    }

    /**
     * Creates a rule that allows the user to snapshot the current dialog state.
     * use case: worker wants to store the current state to show it to another colleague.
     * use case: worker wants to store the current such that he can continue later (takes break; other urgent task)
     *
     * @param rs
     */
    public static void createSnapshotRule(RuleSystem rs) {
        //TODO impl
    }

    /**
     * Creates two rule 'undo' and 'update_undo' that allow the user to jump back in the dialog history.
     * Valid jump points are updated on discrete interactions like speech output, but should be further specified.
     *
     * @param dialog
     * @param lastInteraction
     */
    public static void createUndoRule(Dialog dialog, int lastInteraction) {
        var rs = dialog.getRuleSystem();
        rs.removeRule("undo");
        rs.addRule("undo", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("undo"))
                    .findFirst()
                    .ifPresent(t -> {
                        rs.removeToken(t);
                        //jump one iteration behind the interaction
                        //this jumps to state before the last interaction was done
                        //~ undo last action
                        dialog.rewind(Math.max(0, lastInteraction-1));
                    });
        });
        rs.setPriority("undo", 20);


        rs.removeRule("update_undo");
        rs.addRule("update_undo", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("output_tts")) //TODO filter for other explicit interactions
                    .findFirst()
                    .ifPresent(t -> {
                        createUndoRule(dialog, dialog.getIteration());
                    });
        });
        rs.setPriority("update_undo", 90);
    }

    public static void createUndoRule(Dialog dialog) {
        createUndoRule(dialog, 0);
    }

    public static void createRepeatRule(RuleSystem ruleSystem, String ruleName, String lastTts) {
        //the user can request a repeat up to 10 seconds
        long until = ruleSystem.getClock().convert(Duration.ofSeconds(10)) + ruleSystem.getIteration();
        ruleSystem.addRule(ruleName, (sys) -> {
//            final Pattern pattern = Pattern.compile("[can ]?[you ]?repeat that[ please]?");
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("intent"))
                    .map(t -> (Token<Intent>) t)
                    .filter(t -> t.payload.is("repeat"))
//                    .map(t -> Tuple.of(t, match(pattern, (String) t.payload)))
//                    .filter(tuple -> !tuple.second.isEmpty())
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.removeRule(ruleName);
                        if (sys.getIteration() >= until) {
                            sys.addToken(new Token("output_tts", "I did not say anything."));
                        } else {
                            sys.addToken(new Token("output_tts", lastTts));
                        }

                    });
        });
    }
}
