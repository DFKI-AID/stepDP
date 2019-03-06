package de.dfki.dialog;

import de.dfki.rengine.RuleSystem;
import de.dfki.rengine.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.stream.Stream;

/**
 *
 */
public class MetaDialog {
    private static final Logger log = LoggerFactory.getLogger(MetaDialog.class);

    public static void createGreetingsRule(Dialog dialog) {
        var rs = dialog.getRuleSystem();
        var tagSystem = dialog.getTagSystem();

        rs.addRule("greetings", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "greetings"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.addToken(Token.builder("output_tts").add("utterance", "hello!").build());
                        sys.disable("greetings", Duration.ofSeconds(4));
                    });
        });
        rs.setPriority("greetings", 20);
        tagSystem.addTag("greetings", "meta");
    }

    public static void createInformAnswer(RuleSystem rs, String ruleName, Runnable yes, Runnable no) {
        rs.addRule(ruleName, (ruleSystem) -> {
            ruleSystem.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "answer"))
                    .forEach(t -> {
                        if (t.payloadEquals("content", "confirm")) {
                            yes.run();
                            rs.removeToken(t);
                            rs.removeRule(ruleName);
                        } else if (t.payloadEquals("content", "disconfirm")) {
                            no.run();
                            rs.removeToken(t);
                            rs.removeRule(ruleName);
                        } else {
                            log.warn("malformed 'answer intent': {}", t);
                        }
                    });
        });
        rs.setVolatile(ruleName, true);
        rs.setPriority(ruleName, 25);

        //TODO to avoid a deadlock:
        //TODO [1] it should be checked if there is still a confirmation active -> cancel it
        //TODO [2] use a different rule name and keep both confirmation rules active (only one rule will consume the response)
    }

    /**
     * Helps the user to understand the current context and what can be done.
     * E.g. show example sentences from the SRGS grammar.jsgf, or generate them from the grammar.jsgf
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
                    .filter(t -> t.payloadEquals("intent", "undo"))
                    .findFirst()
                    .ifPresent(t -> {
                        rs.removeToken(t);
                        //jump one iteration behind the interaction
                        //this jumps to state before the last interaction was done
                        //~ undo last action
                        dialog.rewind(Math.max(0, lastInteraction - 1));
                    });
        });
        rs.setPriority("undo", 20);

        rs.removeRule("update_undo");
        rs.addRule("update_undo", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("output_tts")) //TODO filter for other explicit interactions
                    .findFirst()
                    .ifPresent(t -> {
                        int iteration = dialog.getIteration();
                        log.info("Creating undo-jump point on iteration={}", iteration);
                        createUndoRule(dialog, iteration);
                    });
        });
        rs.setPriority("update_undo", 90);
    }

    public static void createUndoRule(Dialog dialog) {
        createUndoRule(dialog, 0);
    }

    public static void createRepeatRule(RuleSystem ruleSystem, String ruleName, String lastTts) {
        //the user can request a repeat up to 10 seconds
        long until = ruleSystem.getClock().convert(Duration.ofSeconds(25)) + ruleSystem.getIteration();
        ruleSystem.addRule(ruleName, (sys) -> {
//            final Pattern pattern = Pattern.compile("[can ]?[you ]?repeat that[ please]?");
            sys.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "repeat"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.removeRule(ruleName);
                        if (sys.getIteration() >= until) {
                            sys.addToken(Token.builder("output_tts")
                                    .add("utterance", "I did not say anything")
                                    .build());
                        } else {
                            sys.addToken(Token.builder("output_tts")
                                    .add("utterance", lastTts)
                                    .build());
                        }
                    });
        });
    }

    public static Stream<Token> filterIntent(String intent, Stream<Token> tokenStream) {
        return tokenStream
                .filter(t -> t.payloadEquals("intent", intent));
    }
}
