package de.dfki.step.dialog;

import de.dfki.step.core.*;
import de.dfki.step.fusion.FusionComponent;
import de.dfki.step.output.PresentationComponent;
import de.dfki.step.rengine.CoordinationComponent;
import de.dfki.step.rengine.RuleSystemComponent;
import de.dfki.step.core.Clock;
import de.dfki.step.rengine.RuleSystem;
import de.dfki.step.core.Token;
import de.dfki.step.core.ClockComponent;
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

    private AtomicBoolean started = new AtomicBoolean(false);

    protected final Map<String, Component> components = new HashMap<>();
    protected final Map<String, Integer> priorityMap = new HashMap<>();

    public Dialog() {
        Clock clock = new Clock(200);
        addComponent("tag", new TagSystemComponent());

        addComponent("snapshot", new SnapshotComponent());
        setPriority("snapshot", 10);
        addComponent("token", new TokenComponent());
        setPriority("token", 11);
        addComponent("input", new InputComponent());
        setPriority("input", 14);
        addComponent("fusion", new FusionComponent());
        setPriority("fusion", 20);
        addComponent("ruleSystem", new RuleSystemComponent(clock));
        setPriority("ruleSystem", 50);
        addComponent("coordinator", new CoordinationComponent());
        setPriority("coordinator", 90);
        addComponent("output", new PresentationComponent());
        setPriority("output", 100);
        addComponent("clock", new ClockComponent(clock));
        setPriority("clock", 110);

    }






    public void init() {
        if(started.getAndSet(true)) {
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

    public void addComponent(String id, Component comp) {
        if(started.get()) {
            throw new IllegalArgumentException("add components after starting is not supported atm");
        }
        components.put(id, comp);
        priorityMap.put(id, 40);
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

        if(!comp.isPresent()) {
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
        return getComponents(RuleSystemComponent.class).get(0).getRuleSystem();
    }

    public Clock getClock() {
        return getComponents(ClockComponent.class).get(0).getClock();
    }

    @Override
    public <T extends Component> List<T> getComponents(Class<T> clazz) {
        return components.values().stream()
                .filter(c -> clazz.isAssignableFrom(c.getClass()))
                .map(c -> (T) c)
                .collect(Collectors.toList());
    }

    @Override
    public void setPriority(String id, int priority) {
        priorityMap.put(id, priority);
    }
}
