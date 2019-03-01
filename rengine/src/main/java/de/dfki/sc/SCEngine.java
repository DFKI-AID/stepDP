package de.dfki.sc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

/**
 *
 */
public class SCEngine {
    private static final Logger log = LoggerFactory.getLogger(SCEngine.class);
    private final StateChart stateChart;
    private String currentState;
    private Map<String, BooleanSupplier> conditions = new HashMap<>();

    public SCEngine(StateChart stateChart) {
        this.stateChart = stateChart;
        reset();
    }

    public void fire(String event) {
        List<Transition> transitions = stateChart.getTransitions(currentState);
        List<Transition> transitionCandidates = transitions.stream()
                .filter(t -> Objects.equals(t.getEvent(), event))
                .filter(t -> !t.hasCond() || checkCondition(t.getCond()))
                .collect(Collectors.toList());


        if (transitionCandidates.isEmpty()) {
            log.warn("no transition candidate found for state {} and event {}", currentState, event);
            return;
        }

        // TODO slect transition randomly?
        // TODO onEnter onExit
        String targetState = transitionCandidates.get(0).getTarget();

        log.info("state change {}->{}", currentState, targetState);
        this.currentState = targetState;
    }

    public boolean checkCondition(String id) {
        if(!conditions.containsKey(id)) {
            log.warn("Can't check condition {}: Not available", id);
            return false;
        }

        boolean conditionFulfilled = conditions.get(id).getAsBoolean();
        return conditionFulfilled;
    }

    public void addCondition(String id, BooleanSupplier condition) {
        if(conditions.containsKey(id)) {
            log.info("Overwriting condition for {}", id);
        } else {
            log.info("Addint new condition for {}", id);
        }
        conditions.put(id, condition);
    }

    public void reset() {
        this.currentState = stateChart.getInitialState();
    }
}
