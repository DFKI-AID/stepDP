package de.dfki.dialog;

import de.dfki.app.TaskBehavior;
import de.dfki.dialog.grammar.GrammarManager;
import de.dfki.rengine.*;
import org.pcollections.HashTreePSet;
import org.pcollections.PSequence;
import org.pcollections.PSet;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public abstract class Dialog implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Dialog.class);

    protected final Clock clock = new Clock(200);
    protected final RuleSystem rs = new RuleSystem(clock);
    protected final TagSystem<String> tagSystem = new TagSystem();
    protected final GrammarManager grammarManager = new GrammarManager();
    protected final Map<String, Behavior> behaviors = new HashMap<>();
    protected final Map<Behavior, Map<Integer, Object>> behaviorSnapshots = new HashMap<>();
    protected final RuleCoordinator ruleCoordinator = new RuleCoordinator();
    private PSet<Token> tokens = HashTreePSet.empty();
    //tokens that are used for the next iteration
    private PSet<Token> waitingTokens = HashTreePSet.empty();


    protected final AtomicInteger snapshotTarget = new AtomicInteger(-1);
    private Map<Integer, RuleSystem.Snapshot> snapshots = new HashMap<>();

    //for testing
    public PSequence outputHistory = TreePVector.empty();


    public RuleSystem getRuleSystem() {
        return rs;
    }

    public TagSystem<String> getTagSystem() {
        return tagSystem;
    }

    public GrammarManager getGrammarManager() {
        return grammarManager;
    }

    public abstract void init();

    public abstract void update();

    public abstract void deinit();

    /**
     * Updates the global grammar.jsgf based on the functions that are currently active
     *
     * @param rs
     */
    public void updateGrammar(RuleSystem rs) {
        synchronized (grammarManager) {
            //TODO: better builder and then swap grammar.jsgf manager instance
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

    public void present(PresentationRequest presentationReq) {
        String output = presentationReq.getContent().toString();

//        String utterance = t.get("utterance").toString();
        System.out.println("System: " + output);
        rs.removeRule("request_repeat_tts");
        MetaFactory.createRepeatRule(this, "request_repeat_tts", output);

        MetaFactory.createSnapshot(this);
        outputHistory = outputHistory.plus(output);
    }

    protected void applySnapshot() {
        int targetSnapshot = snapshotTarget.getAndSet(-1);
        if (targetSnapshot < 0) {
            return;
        }
        clock.setIteration(targetSnapshot);
        RuleSystem.Snapshot rsSnapshot = snapshots.get(targetSnapshot);
        rs.applySnapshot(rsSnapshot);

        for (Behavior behavior : behaviors.values()) {
            var behaviorSnapshot = behaviorSnapshots.get(behavior).get(targetSnapshot);
            behavior.loadSnapshot(behaviorSnapshot);
        }
    }

    protected void createSnapshot(int iteration) {
        snapshots.put(iteration, rs.createSnapshot());
        for (Behavior behavior : behaviors.values()) {
            Object snapshot = behavior.createSnapshot();
            if (!behaviorSnapshots.containsKey(behavior)) {
                behaviorSnapshots.put(behavior, new HashMap<>());
            }
            behaviorSnapshots.get(behavior).put(iteration, snapshot);
        }
    }

    public void rewind(int iteration) {
        this.snapshotTarget.set(iteration);
    }

    public int getIteration() {
        return clock.getIteration();
    }

    @Override
    public void run() {
        init();
        createSnapshot(0);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ruleCoordinator.reset();
                //removing all tokens that were used last round
                waitingTokens = waitingTokens.minusAll(tokens);
                tokens = waitingTokens;
                applySnapshot();
                updateGrammar(rs);
                update();
                rs.update();
                ruleCoordinator.update();
                createSnapshot(clock.getIteration());
                Thread.sleep((long) clock.getRate()); //TODO not precise, but sufficient to start with
                clock.inc();
            } catch (InterruptedException e) {
                log.warn("Dialog update interrupted. Quitting.");
                log.debug("Dialog update interrupted. Quitting.", e);
            }
        }
        deinit();
    }

    protected void addBehavior(String id, TaskBehavior behavior) {
        behaviors.put(id, behavior);
    }

    public Optional<Behavior> getBehavior(String id) {
        return Optional.ofNullable(behaviors.get(id));
    }

    public RuleCoordinator getRuleCoordinator() {
        return ruleCoordinator;
    }

    public PSet<Token> getTokens() {
        return tokens;
    }

    public void addToken(Token token) {
        log.debug("Adding token {}", token);
        waitingTokens = waitingTokens.plus(token);
    }
}
