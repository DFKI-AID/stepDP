package de.dfki.step.dialog;

import de.dfki.step.blackboard.Board;
import de.dfki.step.core.*;
import de.dfki.step.fusion.FusionComponent;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.output.PresentationComponent;
import de.dfki.step.core.CoordinationComponent;
import de.dfki.step.rengine.RuleComponent;
import de.dfki.step.core.Clock;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.core.Token;
import de.dfki.step.core.ClockComponent;
import de.dfki.step.util.Tuple;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 *
 */
public abstract class Dialog implements Runnable, ComponentManager {
    private static final Logger log = LoggerFactory.getLogger(Dialog.class);
    private int defaultPriority = 150;

    private AtomicBoolean started = new AtomicBoolean(false);

    protected final Map<String, Component> components = new HashMap<>();
    protected final Map<String, Integer> priorityMap = new HashMap<>();

    private final Board blackboard = new Board();
    private final KnowledgeBase kb = new KnowledgeBase(this.blackboard);

    public Dialog() {
        Clock clock = new Clock(200);
        addComponent(new TagSystemComponent());

        var defaultComponents = List.of(
                new Tuple<>(new SnapshotComponent(), 100),
                new Tuple<>(new InputComponent(), 200),
                new Tuple<>(new FusionComponent(), 300),
                new Tuple<>(new TokenComponent(), 400),
                new Tuple<>(new RuleComponent(clock), 500),
                new Tuple<>(new CoordinationComponent(), 600),
                new Tuple<>(new PresentationComponent(), 700),
                new Tuple<>(new ClockComponent(clock), 800)
        );

        for (var tup : defaultComponents) {
            addComponent(tup.x);
            setPriority(tup.x.getId(), tup.y);
        }
    }

    public Board getBlackboard() {
        return blackboard;
    }

    public void init() {
        if (started.getAndSet(true)) {
            throw new RuntimeException("already started");
        }
        components.values().forEach(b -> b.init(this));
    }

    /**
     * Updates all components in the order defined by the priority
     */
    public void update() {
        // sort components after priority and update them
        List<Component> sortedComps = components.entrySet().stream()
                .sorted(Comparator.comparing(x -> priorityMap.get(x.getKey())))
                .map(e -> e.getValue())
                .collect(Collectors.toList());
        sortedComps.forEach(c -> c.beforeUpdate());
        sortedComps.forEach(c -> c.update());
        sortedComps.forEach(c -> c.afterUpdate());

        // Update Blackboard
        this.blackboard.update();
    }

    public KnowledgeBase getKB()
    {
        return this.kb;
    }

    public void deinit() {
        components.values().forEach(b -> b.deinit());
    }


    public long getIteration() {
        return getComponents(ClockComponent.class).get(0).getIteration();
    }

    @Override
    public void run() {
        init();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                update();
                Thread.sleep((long) getClock().getRate()); //TODO not precise, but sufficient to start with
            } catch (InterruptedException e) {
                log.warn("Dialog update interrupted. Quitting.");
                log.debug("Dialog update interrupted. Quitting.", e);
                break;
            }
        }
        deinit();
    }

    public void addComponent(Component comp) {
        String id = comp.getId();
        if (started.get()) {
            throw new IllegalArgumentException("add components after starting is not supported atm");
        }
        components.put(id, comp);
        priorityMap.put(id, defaultPriority);
    }

    public Optional<Component> getComponent(String id) {
        return Optional.ofNullable(components.get(id));
    }

    public <T extends Component> Optional<T> getComponent(String id, Class<T> clazz) {
        return Optional.ofNullable(components.get(id))
                .filter(c -> clazz.isAssignableFrom(c.getClass()))
                .map(c -> (T) c);
    }

    public <T extends Component> Optional<T> getComponent(Class<T> clazz) {
        //TODO throw exception if multiple components are found?
        return components.values().stream()
                .filter(c -> clazz.isAssignableFrom(c.getClass()))
                .map(c -> (T) c)
                .findAny();
    }

    public <T extends Component> T retrieveComponent(Class<T> clazz, String errMsg) {
        var comp = components.values().stream()
                .filter(c -> clazz.isAssignableFrom(c.getClass()))
                .map(c -> (T) c)
                .findAny();

        if (!comp.isPresent()) {
            throw new IllegalArgumentException(String.format(
                    "Component %s not available. %s", clazz, errMsg)
            );
        }
        return comp.get();
    }


    public CoordinationComponent getRuleCoordinator() {
        return retrieveComponent(CoordinationComponent.class);
    }

    public SnapshotComponent getSnapshotComp() {
        return retrieveComponent(SnapshotComponent.class);
    }

    public TagSystem<String> getTagSystem() {
        return retrieveComponent(TagSystemComponent.class);
    }

    public PSet<Token> getTokens() {
        return retrieveComponent(TokenComponent.class).getTokens();
    }

    public RuleSystem getRuleSystem() {
        return getComponents(RuleComponent.class).get(0).getRuleSystem();
    }

    public Clock getClock() {
        return getComponents(ClockComponent.class).get(0).getClock();
    }

    @Override
    public synchronized <T extends Component> List<T> getComponents(Class<T> clazz) {
        return components.values().stream()
                .filter(c -> clazz.isAssignableFrom(c.getClass()))
                .map(c -> (T) c)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void setPriority(String id, int priority) {
        priorityMap.put(id, priority);
    }

    @Override
    public synchronized int getPriority(String id) {
        if (!priorityMap.containsKey(id)) {
            return defaultPriority;
        }
        return priorityMap.get(id);
    }

    @Override
    public <T extends Component> Map<String, T> getComponentsMap(Class<T> clazz) {
        Map<String, T> map = components.entrySet().stream()
                .filter(e -> clazz.isAssignableFrom(e.getValue().getClass()))
                .map(e -> (Map.Entry<String, T>) e)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        return map;
    }
}
