package de.dfki.rengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class StateHandler {
    private static final Logger log = LoggerFactory.getLogger(StateHandler.class);
    private String currentState = "None";
    private final TagSystem<String> tagSystem;
    private final Dialog dialog;
    private final Map<String, State> stateMap = new HashMap<>();

    public StateHandler(Dialog dialog) {
        this.dialog = dialog;
        this.tagSystem = dialog.getTagSystem();
    }

    /**
     * Activate the state that related to the given tag
     *
     * @param state
     */
    protected void activate(String state) {
        if (!stateMap.containsKey(state)) {
            return;
        }
        stateMap.get(state).getRules()
                .forEach(r -> dialog.rs.enable(r));
    }

    protected void transfer(String stateId, String targetStateId) {
        Set<String> targetState = stateMap.get(targetStateId).getRules();

        if (stateMap.containsKey(stateId)) {
            var state = stateMap.get(stateId);
            stateMap.get(stateId).getRules()
                    .forEach(r -> {
                        if (targetState.contains(r)) {
                            // skip rule that will also be activate in the next state
                            return;
                        }

                        if (state.isVolatile(stateId)) {
                            dialog.rs.removeRule(r);
                        } else {
                            dialog.rs.disable(r);
                        }
                    });
        }

        activate(targetStateId);
    }

    public void enter(String state) {
        log.info("Entering state {}", state);
        this.transfer(currentState, state);
        currentState = state;
    }

    public void init(String initialState) {
        //disable all rules and activate the rules associated with the intial state
        stateMap.forEach((id, s) -> {
            s.getRules().forEach(r -> {
                dialog.rs.disable(r);
            });
        });
        enter(initialState);
    }


    public State.Builder createState(String id) {
        return new State.Builder(this, id);
    }

    protected void addState(State state) {
        this.stateMap.put(state.getId(), state);
    }
}
