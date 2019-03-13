package de.dfki.step.dialog;

import de.dfki.step.rengine.Clock;
import de.dfki.step.rengine.RuleCoordinator;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.rengine.Token;
import de.dfki.step.srgs.GrammarManager;
import org.pcollections.HashTreePSet;
import org.pcollections.PSequence;
import org.pcollections.PSet;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public abstract class Dialog implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Dialog.class);

    private AtomicBoolean started = new AtomicBoolean(false);
    protected final Clock clock = new Clock(100);
    protected final RuleSystem rs = new RuleSystem(clock);
    protected final TagSystem<String> tagSystem = new TagSystem();
    protected final GrammarManager grammarManager = new GrammarManager();
    protected final Map<String, Behavior> behaviors = new HashMap<>();
    protected final Map<Behavior, Map<Long, Object>> behaviorSnapshots = new HashMap<>();
    protected final RuleCoordinator ruleCoordinator = new RuleCoordinator();
    private PSet<Token> tokens = HashTreePSet.empty();
    //tokens that are used for the next iteration
    private PSet<Token> waitingTokens = HashTreePSet.empty();


    protected final AtomicLong snapshotTarget = new AtomicLong(-1);
    private Map<Long, RuleSystem.Snapshot> snapshots = new HashMap<>();

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

    public void init() {
        if(started.getAndSet(true)) {
            throw new RuntimeException("already started");
        }
        behaviors.values().forEach(b -> b.init(this));
    }

    public void update() {
        ruleCoordinator.reset();
        //removing all tokens that were used last round
        waitingTokens = waitingTokens.minusAll(tokens);
        tokens = waitingTokens;
        applySnapshot();
        updateGrammar(rs);
        rs.update();
        ruleCoordinator.update();
        createSnapshot(clock.getIteration());
        clock.inc();
    }

    public void deinit() {
        behaviors.values().forEach(b -> b.deinit());
    }

    /**
     * Updates the global srgs.jsgf based on the functions that are currently active
     *
     * @param rs
     */
    public void updateGrammar(RuleSystem rs) {
        synchronized (grammarManager) {
            //TODO: better builder and then swap srgs.jsgf manager instance
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
        long targetSnapshot = snapshotTarget.getAndSet(-1);
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

    protected void createSnapshot(long iteration) {
        snapshots.put(iteration, rs.createSnapshot());
        for (Behavior behavior : behaviors.values()) {
            Object snapshot = behavior.createSnapshot();
            if (!behaviorSnapshots.containsKey(behavior)) {
                behaviorSnapshots.put(behavior, new HashMap<>());
            }
            behaviorSnapshots.get(behavior).put(iteration, snapshot);
        }
    }

    public void rewind(long iteration) {
        this.snapshotTarget.set(iteration);
    }

    public long getIteration() {
        return clock.getIteration();
    }

    @Override
    public void run() {
        init();
        createSnapshot(0);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                update();
                Thread.sleep((long) clock.getRate()); //TODO not precise, but sufficient to start with
            } catch (InterruptedException e) {
                log.warn("Dialog update interrupted. Quitting.");
                log.debug("Dialog update interrupted. Quitting.", e);
                break;
            }
        }
        deinit();
    }

    public void addBehavior(String id, Behavior behavior) {
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

    public void addTokens(Collection<Token> tokens) {
        // TODO origin = list of e.g. inputs or random strings
        log.debug("Adding token {}", tokens);
        waitingTokens = waitingTokens.plusAll(tokens);
    }

    public void addToken(Token token) {
        log.debug("Adding token {}", token);
        waitingTokens = waitingTokens.plus(token);
    }

    public Clock getClock() {
        return clock;
    }
}
