package de.dfki.rengine;

import de.dfki.rengine.grammar.GrammarManager;
import de.dfki.rengine.grammar.Item;
import de.dfki.rengine.grammar.OneOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 *
 */
public abstract class Dialog implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Dialog.class);

    protected final RuleSystem rs = new RuleSystem();
    protected final TagSystem<Rule> tagSystem = new TagSystem();
    protected final GrammarManager grammarManager = new GrammarManager();


    public static void createConfirmRule(RuleSystem ruleSystem, String ruleName, Runnable yes, Runnable no) {
        ruleSystem.addRule(ruleName, (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("asr"))
                    .filter(t -> Objects.equals(t.payload, "yes") || Objects.equals(t.payload, "no"))
                    .findFirst()
                    .ifPresent(t -> {
                        if (Objects.equals(t.payload, "yes")) {
                            yes.run();
                        } else {
                            no.run();
                        }
                        sys.removeToken(t);
                        sys.removeRule(ruleName);
                    });
        });
        ruleSystem.setPriority(ruleName, 25);
    }

    public static void createRepeatRule(RuleSystem ruleSystem, String ruleName, String lastTts) {
        //the user can request a repeat up to 10 seconds
        long until = ruleSystem.getClock().convert(Duration.ofSeconds(10)) + ruleSystem.getIteration();
        ruleSystem.addRule(ruleName, (sys) -> {
//            final Pattern pattern = Pattern.compile("[can ]?[you ]?repeat that[ please]?");
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("asr"))
                    .filter(t -> Objects.equals(t.payload, "repeat"))
//                    .map(t -> Tuple.of(t, match(pattern, (String) t.payload)))
//                    .filter(tuple -> !tuple.second.isEmpty())
                    .findFirst()
                    .ifPresent(t -> {
                        sys.removeToken(t);
                        sys.removeRule(ruleName);
                        if(sys.getIteration() >= until) {
                            sys.addToken(new Token("output_tts", "I did not say anything."));
                        } else {
                            sys.addToken(new Token("output_tts", lastTts));
                        }

                    });


        });
    }

    public static void createUndoRule(RuleSystem rs) {
        createUndoRule(rs, 0);
    }

    /**
     * Creates two rule 'undo' and 'update_undo' that allow the user to jump back in the dialog history.
     * Valid jump points are updated on discrete interactions like speech output, but should be further specified.
     *
     * @param rs
     * @param lastInteraction
     */
    public static void createUndoRule(RuleSystem rs, int lastInteraction) {
        rs.removeRule("undo");
        rs.addRule("undo", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("asr"))
                    .filter(t -> Objects.equals(t.payload, "undo"))
                    .findFirst()
                    .ifPresent(t -> {
                        rs.removeToken(t);
                        rs.rewind(lastInteraction);
                    });
        });
        rs.setPriority("undo", 20);

        rs.removeRule("update_undo");
        rs.addRule("update_undo", (sys) -> {
            sys.getTokens().stream()
                    .filter(t -> t.topicIs("output_tts")) //TODO filter for other explicit interactions
                    .findFirst()
                    .ifPresent(t -> {
                        createUndoRule(rs, rs.getIteration());
                    });
        });
        rs.setPriority("update_undo", 90);
    }

    /**
     * Creates a rule that allows the user to snapshot the current dialog state.
     * use case: worker wants to store the current state to show it to another colleague.
     * use case: worker wants to store the current such that he can continue later (takes break; other urgent task)
     * @param rs
     */
    public static void createSnapshotRule(RuleSystem rs) {
        //TODO impl
    }

    /**
     * Helps the user to understand the current context and what can be done.
     * E.g. show example sentences from the SRGS grammar, or generate them from the grammar
     * @param rs
     */
    public static void createHelpRule(RuleSystem rs) {
        //TODO impl
    }

    public RuleSystem getRuleSystem() {
        return rs;
    }

    public TagSystem<Rule> getTagSystem() {
        return tagSystem;
    }

    public GrammarManager getGrammarManager() {
        return grammarManager;
    }

    public abstract void init();

    public abstract void deinit();

    /**
     * Updates the global grammar based on the rules that are currently active
     * @param rs
     */
    public void updateGrammar(RuleSystem rs) {
        synchronized (grammarManager) {
            //TODO: better builder and then swap grammar manager instance
            grammarManager.deactivateAll();
            rs.getRules()
                    .forEach(rule -> {
                        Optional<String> name = rs.getName(rule);
                        if (!name.isPresent()) {
                            return;
                        }
                        grammarManager.setActive(name.get(), rs.isEnabled(rule));
                    });
        }
    }

    @Override
    public void run() {
        init();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                updateGrammar(rs);
                rs.update();
            } catch (InterruptedException e) {
                log.warn("Dialog update interrupted. Quitting.");
                log.debug("Dialog update interrupted. Quitting.", e);
            }
        }
        deinit();
    }
}
