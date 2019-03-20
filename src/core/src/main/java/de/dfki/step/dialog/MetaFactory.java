package de.dfki.step.dialog;

import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.rengine.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
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

        var utterances = List.of("Hello!", "Greetings.", "Hey");
        var rdm = new Random();

        //add new rule with the name 'greetings'
        rs.addRule("greetings", () -> {
            // check for one token with the intent 'greetings'
            dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "greetings"))
                    .findFirst()
                    .ifPresent(t -> {
                        // Create an update function that may get executed later.
                        // This depends on the implementation of the rule coordinator
                        // The .attach call defines that rule wants to consume the given token
                        // If another rules wants to consume the same token, only one rule may be fired.
                        dialog.getRuleCoordinator().add(() -> {
                            String utteranace = utterances.get(rdm.nextInt(utterances.size()));
                            // request tts output via token
                            dialog.present(new PresentationRequest(utteranace));
                            // disable this rule for four seconds
                            dialog.getRuleSystem().disable("greetings", Duration.ofSeconds(4));
                        }).attachOrigin(t);

                    });
        });
        // set the priority of the greetings rule.
        // associate the greetings rule with the meta tag
        tagSystem.addTag("greetings", "meta");
    }

    public static void createInformAnswer(Dialog dialog, String ruleName, Runnable yes, Runnable no) {
        RuleSystem rs = dialog.getRuleSystem();
        dialog.getRuleSystem().addRule(ruleName, () -> {
            dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "answer"))
                    .forEach(t -> {

                        Double confidence = getConfidence(t).orElse(1.0);
                        if (confidence < minConfidence) {
                            log.debug("Ignoring 'answer' intent because confidence is too low.");
                            return;
                        }

                        final Runnable fnc;
                        if (t.payloadEquals("content", "confirm")) {
                            fnc = yes;
                        } else if (t.payloadEquals("content", "disconfirm")) {
                            fnc = no;
                        } else {
                            log.warn("malformed 'answer intent': {}", t);
                            return;
                        }

                        dialog.getRuleCoordinator().add(() -> {
                            fnc.run();
                            rs.removeRule(ruleName);
                        }).attachOrigin(t);
                    });
        });
        rs.setVolatile(ruleName, true);

        //TODO to avoid a deadlock:
        //TODO [1] it should be checked if there is still a confirmation active -> cancel it
        //TODO [2] use a different rule name and keep both confirmation functions active (only one rule will consume the response)
    }

    /**
     * Helps the user to understand the current context and what can be done.
     * E.g. show example sentences from the SRGS srgs.jsgf, or generate them from the srgs.jsgf
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
     * <p>
     * TODO if no name is specified, create a clarify rule / selection
     *
     * @param dialog
     */
    public static void snapshotRule(Dialog dialog) {
        dialog.getRuleSystem().addRule("create_snapshot", () -> {
            Optional<Token> intent = dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "create_snapshot"))
                    .findFirst();

            if (!intent.isPresent()) {
                return;
            }

            String tokenName = intent.get().get("name")
                    .map(s -> tryParse(String.class, s))
                    .orElse("");

            if(tokenName.isEmpty()) {
                //TODO maybe add clarification rule
                return;
            }

            dialog.getRuleCoordinator().add(() -> {
               rewindRule(dialog, tokenName, dialog.getIteration());
            });
        });
        dialog.getTagSystem().addTag("create_snapshot", "meta");
    }

    /**
     * TODO If feature is heavly used, there will be a lot of rewind_x rules. The functionality could also be put into one rule
     *
     * @param dialog
     */
    public static void rewindRule(Dialog dialog, String name, long iteration) {
        String ruleName = "rewind_" + name;
        dialog.getRuleSystem().addRule(ruleName, () -> {
            Optional<Token> rewindIntent = dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "rewind"))
                    .findFirst();
            if (!rewindIntent.isPresent()) {
                return;
            }

            String tokenName = rewindIntent.get().get("name")
                    .map(s -> tryParse(String.class, s))
                    .orElse("");
            if(tokenName.isEmpty()) {
                //TODO maybe add clarification rule
                return;
            }

            if(!Objects.equals(name, tokenName)) {
                //other point in time
                return;
            }

            dialog.getRuleCoordinator().add(() -> {
                dialog.rewind(iteration);
            }).attachOrigin(rewindIntent.get());
        });
        dialog.getTagSystem().addTag(ruleName, "meta");
    }

    /**
     * Creates the rule 'undo' that allow the user to jump back in the dialog history.
     * Valid jump points should be created on meaningful points of the dialog.
     * See {@link #createSnapshot}
     *
     * @param dialog
     * @param lastInteraction
     */
    public static void createUndoRule(Dialog dialog, long lastInteraction) {
        var rs = dialog.getRuleSystem();
        rs.removeRule("undo");
        rs.addRule("undo", () -> {
            dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "undo"))
                    .findFirst()
                    .ifPresent(t -> {

                        dialog.getRuleCoordinator().add(() -> {
                            //jump one iteration behind the interaction
                            //this jumps to state before the last interaction was done
                            //~ undo last action
                            dialog.rewind(Math.max(0, lastInteraction - 1));
                        }).attachOrigin(t);
                    });
        });
        dialog.getTagSystem().addTag("undo", "meta");
    }

    public static void createSnapshot(Dialog dialog) {
        long iteration = dialog.getIteration();
        log.info("Creating undo-jump point on iteration={}", iteration);
        createUndoRule(dialog, iteration);
    }

    public static void createUndoRule(Dialog dialog) {
        createUndoRule(dialog, 0);
    }

    public static void createRepeatRule(Dialog dialog, String ruleName, String lastTts) {
        String prefix = "I said ";
        RuleSystem rs = dialog.getRuleSystem();
        //the user can request a repeat up to 10 seconds
        long until = rs.getClock().convert(Duration.ofSeconds(25)) + rs.getIteration();
        rs.addRule(ruleName, () -> {
//            final Pattern pattern = Pattern.compile("[can ]?[you ]?repeat that[ please]?");
            dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "repeat"))
                    .findFirst()
                    .ifPresent(t -> {

                        dialog.getRuleCoordinator().add(() -> {
                            rs.removeRule(ruleName);
                            if (rs.getIteration() >= until) {
                                dialog.present(new PresentationRequest("I did not say anything"));
                            } else {
                                dialog.present(new PresentationRequest(prefix + lastTts));
                            }
                        }).attachOrigin(t);
                    });
        });
        dialog.getTagSystem().addTag(ruleName, "meta");
    }

    public static Stream<Token> filterIntent(String intent, Stream<Token> tokenStream) {
        return tokenStream
                .filter(t -> t.payloadEquals("intent", intent));
    }

    public static void selectRule(Dialog dialog, String ruleName, List<String> choices, Consumer<String> callback) {
        RuleSystem rs = dialog.getRuleSystem();
        //TODO use 'choices' to update srgs
        rs.addRule(ruleName, () -> {
            Optional<Token> selectToken = dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "select"))
                    .findFirst();

            if (!selectToken.isPresent()) {
                return;
            }

            Optional<String> selection = selectToken.get().get("selection")
                    .filter(s -> s instanceof String)
                    .map(s -> (String) s);


            Optional<Double> confidence = selectToken.get().get("confidence")
                    .filter(c -> c instanceof Double)
                    .map(c -> (Double) c);

            if (!selection.isPresent()) {
                dialog.getRuleCoordinator().add(() -> {
                    //TODO nlg for question
                    dialog.present(new PresentationRequest("Which TODO do you mean?"));
                    specifyRule(dialog, "specify_" + ruleName, callback);

                }).attachOrigin(selectToken.get());
                return;
            }

            if (confidence.isPresent() && confidence.get() < minConfidence) {
                dialog.getRuleCoordinator().add(() -> {
                    PresentationRequest pr = new PresentationRequest("Please confirm your selection for " + selection.get());
                    dialog.present(pr);

                    MetaFactory.createInformAnswer(dialog, "confirm_" + ruleName,
                            () -> callback.accept(selection.get()),
                            () -> {
                            }
                    );
                }).attachOrigin(selectToken.get());


            } else {
                dialog.getRuleCoordinator().add(() -> {
                    callback.accept(selection.get());
                }).attachOrigin(selectToken.get());
            }

        });
    }


    /**
     * If the system can't derive the intended task referred by the user, the user
     * may specify his request by "the first task"
     * <p>
     * TODO return whole token in callback
     */
    public static void specifyRule(Dialog dialog, String rule, Consumer<String> callback) {
        var rs = dialog.getRuleSystem();
        rs.addRule(rule, () -> {
            dialog.getTokens().stream()
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

                        dialog.getRuleCoordinator().add(() -> {
                            rs.removeRule(rule);
                            String specificationStr = (String) specification.get();
                            callback.accept(specificationStr);
                        }).attachOrigin(t);
                    });
        });
    }


    /**
     * e.g. user interupts the system via gesture or speech input
     *
     * @param dialog
     * @param ruleName
     * @param callback consumer that the intent specifying the turn_grab
     */
    public static void turnGrabRule(Dialog dialog, String ruleName, Consumer<Token> callback) {
        dialog.getRuleSystem().addRule(ruleName, () -> {
            dialog.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "turn_grab"))
                    .findFirst()
                    .ifPresent(t -> {
                        dialog.getRuleCoordinator().add(() -> {
                            callback.accept(t);
                        }).attachOrigin(t);
                    });
        });
        dialog.getTagSystem().addTag(ruleName, "meta");
    }

    public static Optional<Double> getConfidence(Token token) {
        return token.get("confidence")
                .filter(c -> c instanceof Double)
                .map(c -> (Double) c);
    }

    /**
     * Creates a rule that triggers the given callback after some time elapsed
     *
     * @param dialog
     * @param name
     * @param callback
     */
    public static void timeoutRule(Dialog dialog, String name, Duration duration, Runnable callback) {
        long currentIteration = dialog.getIteration();
        long untilIteration = currentIteration + dialog.getClock().convert(duration);

        dialog.getRuleSystem().addRule(name, () -> {
            if (dialog.getIteration() < untilIteration) {
                return;
            }

            dialog.getRuleCoordinator().add(() -> {
                dialog.getRuleSystem().removeRule(name);
                callback.run();
            });
        });
    }

    public static <T> T tryParse(Class<T> clazz, Object obj) {
        if(!clazz.isAssignableFrom(obj.getClass())) {
            return null;
        }

        return (T) obj;
    }

    /**
     * e.g. MADMACS demo: following the visual focus: user looks on the car, then on the billboard
     * @param dialog
     * @param name
     * @param supp
     * @param values
     * @param <T>
     */
    public static <T> void cascadeRule(Dialog dialog, String name, Supplier<T> supp, List<T> values) {
        throw new UnsupportedOperationException("not impl");
    }
}
