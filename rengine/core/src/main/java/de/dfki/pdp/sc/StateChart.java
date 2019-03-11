package de.dfki.pdp.sc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 */
public class StateChart {
    private static final Logger log = LoggerFactory.getLogger(StateChart.class);
    private State root;
    private String initialState;
    private Map<String, State> states = new HashMap<>();

    public State getRoot() {
        return root;
    }

    public String getInitialState() {
        return initialState;
    }

    protected void setRoot(State root) {
        this.root = root;
        fillStateMap(root);
    }

    protected void fillStateMap(State state) {
        states.put(state.getId(), state);
        state.getChildren().forEach(s -> fillStateMap(s));
    }

    protected void setInitialState(String initialState) {
        this.initialState = initialState;
    }

    public List<Transition> getTransitions(String state) {
        State s = states.get(state);
        if (s == null) {
            log.warn("no state found with the id {}", state);
            return Collections.EMPTY_LIST;
        }
        return s.getTransitions();
    }

    public Collection<State> getStates() {
        return states.values();
    }

    /**
     * @param state
     * @param event
     * @return A set of all transition for the next state
     */
    public Set<String> fire(String state, String event) {
        State s = states.get(state);
        if (s == null) {
            log.warn("no state found with the id {}", state);
            return Collections.EMPTY_SET;
        }

        Set<String> targetStates = new HashSet<>();
        for (Transition transition : s.getTransitions()) {
            if (!Objects.equals(transition.getEvent(), event)) {
                continue;
            }

            targetStates.add(transition.getTarget());
        }
        return targetStates;
    }

    public Optional<State> getState(String currentState) {
        return Optional.ofNullable(states.get(currentState));
    }
}
