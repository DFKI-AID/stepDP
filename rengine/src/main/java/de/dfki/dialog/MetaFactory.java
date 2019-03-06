package de.dfki.dialog;

import de.dfki.app.TaskBehavior;
import de.dfki.rengine.RuleSystem;
import de.dfki.rengine.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 */
public class MetaFactory {
    private static final Logger log = LoggerFactory.getLogger(MetaFactory.class);
    private static final double minConfidence = 0.3;

    public static void createGreetingsRule(Dialog dialog) {
        var rs = dialog.getRuleSystem();
        var tagSystem = dialog.getTagSystem();

        rs.addRule("greetings", (sys) -> {
            // check for tokens with the intent 'greetings'
            sys.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "greetings"))
                    .findFirst()
                    .ifPresent(t -> {
                        // consume the token (subsequent rules won't see the token)
                        sys.removeToken(t);
                        // request tts output via token
                        sys.addToken(Token.builder("output_tts").add("utterance", "hello!").build());
                        // disable this rule for four seconds
                        sys.disable("greetings", Duration.ofSeconds(4));
                    });
        });
        // set the priority of the greetings rule.
        rs.setPriority("greetings", 20);
        // associate the greetings rule with the meta tag
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
        throw new UnsupportedOperationException("not impl");
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

        //if no name is specified, create a clarify rule / selection

        throw new UnsupportedOperationException("not impl");
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

    public static void selectRule(RuleSystem rs, String ruleName, List<String> choices, Consumer<String> callback) {
        //TODO use 'choices' to update grammar
        rs.addRule(ruleName, (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "select"))
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);

                        Optional<String> selection = t.get("selection")
                                .filter(s -> s instanceof String)
                                .map(s -> (String) s);


                        Optional<Double> confidence = t.get("confidence")
                                .filter(c -> c instanceof Double)
                                .map(c -> (Double) c);

                        if (selection.isPresent()) {
                            if (confidence.isPresent() && confidence.get() < minConfidence) {
                                System.out.println("Please confirm your selection for " + selection.get());
                                MetaFactory.createInformAnswer(sys, "confirm_" + ruleName,
                                        () -> callback.accept(selection.get()),
                                        () -> {
                                        }
                                );
                            } else {
                                callback.accept(selection.get());
                            }
                        } else {
                            //TODO nlg for question
                            sys.addToken(new Token("output_tts").add("utterance", "which TODO do you mean?"));
                            specifyRule(rs, "specify_" + ruleName, callback);
                        }
                    });
        });
        rs.setPriority("select_task", 20);
    }


    /**
     * If the system can't derive the intended task referred by the user, the user
     * may specify his request by "the first task"
     */
    public static void specifyRule(RuleSystem rs, String rule, Consumer<String> callback) {
        rs.addRule(rule, (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "specify"))
                    .findFirst()
                    .ifPresent(t -> {
                        Optional<String> specification = t.get("specification")
                                .filter(s -> s instanceof String)
                                .map(s -> (String) s);
                        if (!specification.isPresent()) {
                            log.warn("no 'specification' info available. missing tag?");
                            return;
                        }

                        sys.removeToken(t);
                        rs.removeRule(rule);
                        String specificationStr = (String) specification.get();
                        callback.accept(specificationStr);
                    });
        });
        rs.setPriority(rule, 20);
    }
}
