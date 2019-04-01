package de.dfki.step.sc;

import de.dfki.step.core.TagSystem;
import de.dfki.step.rengine.RuleSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO fire onEnter for intial state
 */
public class SCHandler {
    private static final Logger log = LoggerFactory.getLogger(SCHandler.class);
    private final SCEngine engine;
    private final TagSystem<String> tagSystem;
    private final RuleSystem rs;

    public SCHandler(RuleSystem rs, TagSystem<String> tagSystem, SCEngine engine) {
        this.rs = rs;
        this.tagSystem = tagSystem;
        this.engine = engine;
    }

    public void init() {
        engine.reset();
        update();
    }

    public void fire(String event) {
        if (!engine.fire(event)) {
            return;
        }
        update();
    }

    protected void update() {
        String currentState = engine.getCurrentState();
        List<String> otherStates = engine.getStates()
                .stream()
                .filter(s -> !s.equals(currentState)).collect(Collectors.toList());


        otherStates.forEach(s -> deactivate(s));
        activate(currentState);
    }

    /**
     * Activates all functions with the given tag
     *
     * @param tag
     */
    protected void activate(String tag) {
        rs.getRules().stream()
                .map(r -> rs.getName(r))
                .filter(r -> r.isPresent())
                .map(r -> r.get())
                .filter(r -> tagSystem.hasTag(r, tag))
                .forEach(r -> rs.enable(r));
    }

    /**
     * Deactivates all functions with the given tag.
     *
     * @param tag
     */
    protected void deactivate(String tag) {
        rs.getRules().stream()
                .map(r -> rs.getName(r))
                .filter(r -> r.isPresent())
                .map(r -> r.get())
                .filter(r -> tagSystem.hasTag(r, tag))
                .forEach(r -> {
                    rs.disable(r);
                    if (rs.isVolatile(r)) {
                        rs.removeRule(r);
                    }
                });
    }


    public SCEngine.ObjState createSnapshot() {
        return engine.createSnapshot();
    }

    public void loadSnapshot(SCEngine.ObjState objState) {
        engine.loadSnapshot(objState);
    }

    public String getCurrentState() {
        return engine.getCurrentState();
    }

    public SCEngine getEngine() {
        return engine;
    }

    public Collection<String> getStates() {
        return engine.getStates();
    }

    /**
     * quits all states.
     */
//    public void quit() {
//        log.info("Quitting all states.");
//        tags.forEach(t -> deactivate(t));
//        currentState = "None";
//    }

//    public void enter(String state) {
//        if (Objects.equals(state, currentState)) {
//            return;
//        }
//        log.info("Entering state {}", state);
//        tags.forEach(t -> deactivate(t));
//        activate(state);
//        currentState = state;
//    }

}
