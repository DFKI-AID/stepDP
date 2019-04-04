package de.dfki.step.dialog;

import de.dfki.step.core.*;
import de.dfki.step.fusion.FusionComponent;
import de.dfki.step.fusion.FusionNode;
import de.dfki.step.fusion.InputNode;
import de.dfki.step.output.PresentationComponent;
import de.dfki.step.rengine.CoordinationComponent;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.rengine.RuleSystemComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Helper class for creatin rules and fusion nodes for recurring dialog patterns
 */
public class MetaFactory {
    private static final Logger log = LoggerFactory.getLogger(MetaFactory.class);
    private static final double minConfidence = 0.3;
    private final RuleSystemComponent rs;
    private final TokenComponent tc;
    private final CoordinationComponent rc;
    private final PresentationComponent pc;
    private final ClockComponent cc;
    private final TagSystemComponent tsc;
    private final SnapshotComponent sc;
    private final FusionComponent fc;

    public MetaFactory(ComponentManager cm) {
        rs = cm.retrieveComponent(RuleSystemComponent.class);
        tc = cm.retrieveComponent(TokenComponent.class);
        rc = cm.retrieveComponent(CoordinationComponent.class);
        pc = cm.retrieveComponent(PresentationComponent.class);
        cc = cm.retrieveComponent(ClockComponent.class);
        tsc = cm.retrieveComponent(TagSystemComponent.class);
        sc = cm.retrieveComponent(SnapshotComponent.class);
        fc = cm.retrieveComponent(FusionComponent.class);
    }

    public void createGreetingsRule() {

        var utterances = List.of("Hello!", "Greetings.", "Hey");
        var rdm = new Random();

        //add new rule with the name 'greetings'
        rs.addRule("greetings", () -> {
            // check for one token with the intent 'greetings'
            tc.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "greetings"))
                    .findFirst()
                    .ifPresent(t -> {
                        // Create an update function that may get executed later.
                        // This depends on the implementation of the rule coordinator
                        // The .attach call defines that rule wants to consume the given token
                        // If another rules wants to consume the same token, only one rule may be fired.
                        rc.add(() -> {
                            String utteranace = utterances.get(rdm.nextInt(utterances.size()));
                            // request tts output via token
                            tts(utteranace);
                            // disable this rule for four seconds
                            rs.disable("greetings", Duration.ofSeconds(4));
                        }).attachOrigin(t);

                    });
        });
        // set the priority of the greetings rule.
        // associate the greetings rule with the meta tag
        tsc.addTag("greetings", "meta");
    }

    public void createInformAnswer(String ruleName, Runnable yes, Runnable no) {
        rs.addRule(ruleName, () -> {
            tc.getTokens().stream()
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

                        rc.add(() -> {
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
     * TODO if no name is specified, of a clarify rule / selection
     *
     */
    public void snapshotRule() {
        rs.addRule("create_snapshot", () -> {
            Optional<Token> intent = tc.getTokens().stream()
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

            rc.add(() -> {
               rewindRule(tokenName, cc.getIteration());
            });
        });
        tsc.addTag("create_snapshot", "meta");
    }

    /**
     * TODO If feature is heavly used, there will be a lot of rewind_x rules. The functionality could also be put into one rule
     *
     */
    public void rewindRule(String name, long iteration) {
        String ruleName = "rewind_" + name;
        rs.addRule(ruleName, () -> {
            Optional<Token> rewindIntent = tc.getTokens().stream()
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

            rc.add(() -> {
                sc.rewind(iteration);
            }).attachOrigin(rewindIntent.get());
        });
        tsc.addTag(ruleName, "meta");
    }

    /**
     * Creates the rule 'undo' that allow the user to jump back in the dialog history.
     * Valid jump points should be created on meaningful points of the dialog.
     * See {@link #createSnapshot}
     *
     * @param lastInteraction
     */
    public void createUndoRule(long lastInteraction) {
        rs.removeRule("undo");
        rs.addRule("undo", () -> {
            tc.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "undo"))
                    .findFirst()
                    .ifPresent(t -> {

                        rc.add(() -> {
                            //jump one iteration behind the interaction
                            //this jumps to state before the last interaction was done
                            //~ undo last action
                            sc.rewind(Math.max(0, lastInteraction - 1));
                        }).attachOrigin(t);
                    });
        });
        tsc.addTag("undo", "meta");
    }

    public void createSnapshot() {
        long iteration = cc.getIteration();
        log.info("Creating undo-jump point on iteration={}", iteration);
        createUndoRule(iteration);
    }

    public void createUndoRule() {
        createUndoRule( 0);
    }

    public void createRepeatRule(String ruleName, String lastTts) {
        String prefix = "I said ";
        //the user can request a repeat up to 10 seconds

        long until = cc.convert(Duration.ofSeconds(25)) + rs.getIteration();
        rs.addRule(ruleName, () -> {
//            final Pattern pattern = Pattern.compile("[can ]?[you ]?repeat that[ please]?");
            tc.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "repeat"))
                    .findFirst()
                    .ifPresent(t -> {

                        rc.add(() -> {
                            rs.removeRule(ruleName);
                            if (rs.getIteration() >= until) {
                                tts("I did not say anything");
                            } else {
                                tts(prefix + lastTts);
                            }
                        }).attachOrigin(t);
                    });
        });
        tsc.addTag(ruleName, "meta");
    }

    public static Stream<Token> filterIntent(String intent, Stream<Token> tokenStream) {
        return tokenStream
                .filter(t -> t.payloadEquals("intent", intent));
    }

    public void selectRule(String ruleName, List<String> choices, Consumer<String> callback) {
        //TODO use 'choices' to update srgs



        rs.addRule(ruleName, () -> {
            Optional<Token> selectToken = tc.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "select"))
                    .findFirst();

            if (!selectToken.isPresent()) {
                return;
            }

            Optional<String> selection = selectToken.get().get("selection", String.class);

            Double confidence = selectToken.get().get("confidence", Double.class)
                    .orElse(1.0);

            if (!selection.isPresent()) {
                rc.add(() -> {
                    //TODO nlg for question
                    tts("Which TODO do you mean?");
                    specifyRule("specify_" + ruleName, callback);
                    //specifyFusion("specify_" + ruleName, choices);

                }).attachOrigin(selectToken.get());
                return;
            }

            if (confidence < minConfidence) {
                rc.add(() -> {
                    String utterance = "Please confirm your selection for " + selection.get();
                    tts(utterance);

                    createInformAnswer( "confirm_" + ruleName,
                            () -> callback.accept(selection.get()),
                            () -> {
                            }
                    );
                }).attachOrigin(selectToken.get());


            } else {
                rc.add(() -> {
                    callback.accept(selection.get());
                    //rs.removeRule(ruleName);
                }).attachOrigin(selectToken.get());
            }

        });
    }

    protected void tts(String utterance) {
        pc.present(PresentationComponent.simpleTTS(utterance));
    }

    public void specifyFusion(String name, List<String> choices) {
        Schema speechFocus = Schema.builder()
                .equalsOneOf(Schema.Key.of("speech_focus"), choices)
                .build();
        FusionNode fusionNode = new InputNode(speechFocus);
        fc.addFusionNode(name, fusionNode, match -> {
            var intent = FusionComponent.defaultIntent(match, "specify");
            List<String> speechFocusList = Token.mergeFields("speech_focus", String.class, match.getTokens());
            if(speechFocusList.size() != 1) {
                throw new IllegalArgumentException("found speech_focus in multiple focus. todo: impl resolve which one to take");
            }

            intent = intent.add("selection", speechFocusList.get(0));
            return intent;
        });

        //TODO "this one" + focus
    }

    /**
     * If the system can't derive the intended task referred by the user, the user
     * may specify his request by an incomplete utterance like "the second task"
     * <p>
     * TODO return whole token in callback
     */
    public void specifyRule(String ruleName, Consumer<String> callback) {
        rs.addRule(ruleName, () -> {
            tc.getTokens().stream()
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

                        rc.add(() -> {
                            rs.removeRule(ruleName);
                            String specificationStr = (String) specification.get();
                            callback.accept(specificationStr);
                            fc.removeFusionNode(ruleName);
                        }).attachOrigin(t);
                    });
        });
    }


    /**
     * e.g. user interupts the system via gesture or speech input
     *
     * @param ruleName
     * @param callback consumer that the intent specifying the turn_grab
     */
    public void turnGrabRule(String ruleName, Consumer<Token> callback) {
        rs.addRule(ruleName, () -> {
            tc.getTokens().stream()
                    .filter(t -> t.payloadEquals("intent", "turn_grab"))
                    .findFirst()
                    .ifPresent(t -> {
                        rc.add(() -> {
                            callback.accept(t);
                        }).attachOrigin(t);
                    });
        });
        tsc.addTag(ruleName, "meta");
    }

    public static Optional<Double> getConfidence(Token token) {
        return token.get("confidence")
                .filter(c -> c instanceof Double)
                .map(c -> (Double) c);
    }

    /**
     * Creates a rule that triggers the given callback after some time elapsed
     *
     * @param cm
     * @param name
     * @param callback
     */
    public static void timeoutRule(ComponentManager cm, String name, Duration duration, Runnable callback) {
        var rs = cm.retrieveComponent(RuleSystemComponent.class);
        var cc = cm.retrieveComponent(ClockComponent.class);
        var rc = cm.retrieveComponent(CoordinationComponent.class);

        long currentIteration = cc.getIteration();
        long untilIteration = currentIteration + cc.convert(duration);

        rs.addRule(name, () -> {
            if (cc.getIteration() < untilIteration) {
                return;
            }

            rc.add(() -> {
                rs.removeRule(name);
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
