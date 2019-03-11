package de.dfki.pdp.dialog;

import de.dfki.pdp.sc.SCEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO fire onEnter for intial state
 */
public class SCHandler {
    private static final Logger log = LoggerFactory.getLogger(SCHandler.class);
    private final Dialog dialog;
    private final SCEngine engine;

    public SCHandler(Dialog dialog, SCEngine engine) {
        this.dialog = dialog;
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
        TagSystem<String> tagSystem = dialog.getTagSystem();
        dialog.rs.getRules().stream()
                .map(r -> dialog.rs.getName(r))
                .filter(r -> r.isPresent())
                .map(r -> r.get())
                .filter(r -> tagSystem.hasTag(r, tag))
                .forEach(r -> dialog.rs.enable(r));
    }

    /**
     * Deactivates all functions with the given tag
     *
     * @param tag
     */
    protected void deactivate(String tag) {
        TagSystem<String> tagSystem = dialog.getTagSystem();
        dialog.rs.getRules().stream()
                .map(r -> dialog.rs.getName(r))
                .filter(r -> r.isPresent())
                .map(r -> r.get())
                .filter(r -> tagSystem.hasTag(r, tag))
                .forEach(r -> {
                    dialog.rs.disable(r);
                    if (dialog.rs.isVolatile(r)) {
                        dialog.rs.removeRule(r);
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
