package de.dfki.step.dialog;

import de.dfki.step.rengine.Clock;
import de.dfki.step.rengine.RuleCoordinator;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.rengine.Token;
import org.pcollections.HashTreePSet;
import org.pcollections.PSequence;
import org.pcollections.PSet;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 *
 */
public abstract class Dialog implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Dialog.class);

    private AtomicBoolean started = new AtomicBoolean(false);
    protected final Clock clock = new Clock(100);
    protected final RuleSystem ruleSystem = new RuleSystem(clock);
    protected final TagSystem<String> tagSystem = new TagSystem();
    protected final Map<String, Component> components = new HashMap<>();
    protected final Map<Component, Map<Long, Object>> behaviorSnapshots = new HashMap<>();
    protected final RuleCoordinator ruleCoordinator = new RuleCoordinator();
    private PSet<Token> tokens = HashTreePSet.empty();
    //tokens that are used for the next iteration
    private PSet<Token> waitingTokens = HashTreePSet.empty();

    protected final AtomicLong snapshotTarget = new AtomicLong(-1);
    private Map<Long, RuleSystem.Snapshot> snapshots = new HashMap<>();

    //for testing
    public PSequence outputHistory = TreePVector.empty();


    public RuleSystem getRuleSystem() {
        return ruleSystem;
    }

    public TagSystem<String> getTagSystem() {
        return tagSystem;
    }

    public void init() {
        if(started.getAndSet(true)) {
            throw new RuntimeException("already started");
        }
        components.values().forEach(b -> b.init(this));
    }

    public void update() {
        //removing all tokens that were used last round
        synchronized(this) {
            waitingTokens = waitingTokens.minusAll(tokens);
            tokens = waitingTokens;
        }
        applySnapshot();

        ruleCoordinator.reset();
        components.values().forEach(c -> c.beforeUpdate());
        components.values().forEach(c -> c.update());
        ruleSystem.update();
        ruleCoordinator.update();
        components.values().forEach(c -> c.afterUpdate());
        createSnapshot(clock.getIteration());
        clock.inc();
    }

    public void deinit() {
        components.values().forEach(b -> b.deinit());
    }



    public void present(PresentationRequest presentationReq) {
        String output = presentationReq.getContent().toString();

//        String utterance = t.getAny("utterance").toString();
        System.out.println("System: " + output);
        ruleSystem.removeRule("request_repeat_tts");
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
        ruleSystem.applySnapshot(rsSnapshot);

        for (Component behavior : components.values()) {
            var behaviorSnapshot = behaviorSnapshots.get(behavior).get(targetSnapshot);
            behavior.loadSnapshot(behaviorSnapshot);
        }
    }

    protected void createSnapshot(long iteration) {
        snapshots.put(iteration, ruleSystem.createSnapshot());
        for (Component behavior : components.values()) {
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

    public void addComponent(String id, Component behavior) {
        components.put(id, behavior);
    }

    public Optional<Component> getComponent(String id) {
        return Optional.ofNullable(components.get(id));
    }

    public <T extends Component> Optional<T> getComponent(String id, Class<T> clazz) {
        return Optional.ofNullable(components.get(id))
                .filter(c -> clazz.isAssignableFrom(c.getClass()))
                .map(c -> (T) c);
    }

    public <T extends Component> List<T> getComponents(Class<T> clazz) {
        return this.components.values().stream()
                .filter(c -> clazz.isAssignableFrom(c.getClass()))
                .map(c -> (T) c)
                .collect(Collectors.toList());
    }


    public RuleCoordinator getRuleCoordinator() {
        return ruleCoordinator;
    }

    public PSet<Token> getTokens() {
        return tokens;
    }

    public synchronized void addTokens(Collection<Token> tokens) {
        // TODO origin = list of e.g. inputs or random strings
        log.debug("Adding token {}", tokens);
        waitingTokens = waitingTokens.plusAll(tokens);
    }

    public synchronized void addToken(Token token) {
        log.debug("Adding token {}", token);
        waitingTokens = waitingTokens.plus(token);
    }

    public Clock getClock() {
        return clock;
    }
}
