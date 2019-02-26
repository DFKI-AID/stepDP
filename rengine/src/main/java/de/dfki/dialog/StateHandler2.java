package de.dfki.dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 */
public class StateHandler2 {
    private static final Logger log = LoggerFactory.getLogger(StateHandler2.class);
    private String currentState = "None";
    private final TagSystem<String> tagSystem;
    private final Dialog dialog;
    private final Set<String> tags;

    public StateHandler2(Dialog dialog, String... tags) {
        this.dialog = dialog;
        this.tagSystem = dialog.getTagSystem();
        this.tags = Set.of(tags);
    }

    /**
     * Activates all rules with the given tag
     *
     * @param tag
     */
    protected void activate(String tag) {
        dialog.rs.getRules().stream()
                .map(r -> dialog.rs.getName(r))
                .filter(r -> r.isPresent())
                .map(r -> r.get())
                .filter(r -> tagSystem.hasTag(r, tag))
                .forEach(r -> dialog.rs.enable(r));
    }

    /**
     * Deactivates all rules with the given tag
     *
     * @param tag
     */
    protected void deactivate(String tag) {
        dialog.rs.getRules().stream()
                .map(r -> dialog.rs.getName(r))
                .filter(r -> r.isPresent())
                .map(r -> r.get())
                .filter(r -> tagSystem.hasTag(r, tag))
                .forEach(r -> {
                    dialog.rs.disable(r);
                    if(dialog.rs.isVolatile(r)) {
                        dialog.rs.removeRule(r);
                    }
                });
    }

    public void enter(String state) {
        if (Objects.equals(state, currentState)) {
            return;
        }
        log.info("Entering state {}", state);
        tags.forEach(t -> deactivate(t));
        activate(state);
        currentState = state;
    }

    public void init(String initialState) {
        tags.forEach(t -> deactivate(t));
        enter(initialState);
    }

}
