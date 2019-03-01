package de.dfki.sc;

import org.pcollections.HashTreePMap;
import org.pcollections.IntTreePMap;
import org.pcollections.PMap;
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
    private ObjState objState = new ObjState("N/A");

    public static class ObjState {
        private final String currentState;
        private PMap<String, BooleanSupplier> conditions = HashTreePMap.empty();
        private PMap<String, Runnable> onEntries = HashTreePMap.empty();

        public ObjState(String currentState) {
            this.currentState = currentState;
        }

        public ObjState setState(String state) {
            ObjState newState = new ObjState(state);
            newState.conditions = conditions;
            newState.onEntries = onEntries;
            return newState;
        }

        public ObjState addCondition(String id, BooleanSupplier condition) {
            ObjState newState = new ObjState(currentState);
            newState.conditions = conditions.plus(id, condition);
            newState.onEntries = onEntries;
            return newState;
        }

        public ObjState addOnEntry(String id, Runnable onEntry) {
            ObjState newState = new ObjState(currentState);
            newState.conditions = conditions;
            newState.onEntries = onEntries.plus(id, onEntry);
            return newState;
        }
    }

    public ObjState createSnapshot() {
        return objState;
    }

    public void loadSnapshot(ObjState objState) {
        this.objState = objState;
    }


    public SCEngine(StateChart stateChart) {
        this.stateChart = stateChart;
        reset();
    }

    public boolean fire(String event) {
        List<Transition> transitions = stateChart.getTransitions(getCurrentState());
        List<Transition> transitionCandidates = transitions.stream()
                .filter(t -> Objects.equals(t.getEvent(), event))
                .filter(t -> !t.hasCond() || checkCondition(t.getCond()))
                .collect(Collectors.toList());


        if (transitionCandidates.isEmpty()) {
            log.warn("no transition candidate found for state {} and event {}", getCurrentState(), event);
            return false;
        }

        // TODO slect transition randomly?
        // TODO onExit
        String targetState = transitionCandidates.get(0).getTarget();
        log.info("state change {}->{}", getCurrentState(), targetState);
        objState = objState.setState(targetState);

        State state = stateChart.getState(getCurrentState()).get();
        state.getOnEntries().forEach(oe -> oe.getScripts().forEach(s -> {
            if (!objState.onEntries.containsKey(s)) {
                log.warn("No script found for on-entry {} and id {}", getCurrentState(), s);
                return;
            }
            objState.onEntries.get(s).run();
        }));

        return true;
    }

    public String getCurrentState() {
        return objState.currentState;
    }

    public Collection<String> getStates() {
        return stateChart.getStates().stream().map(s -> s.getId()).collect(Collectors.toList());
    }

    public boolean checkCondition(String id) {
        if (!objState.conditions.containsKey(id)) {
            log.warn("Can't check condition {}: Not available", id);
            return false;
        }

        boolean conditionFulfilled = objState.conditions.get(id).getAsBoolean();
        return conditionFulfilled;
    }

    public void addCondition(String id, BooleanSupplier condition) {
        if (objState.conditions.containsKey(id)) {
            log.info("Overwriting condition for {}", id);
        } else {
            log.info("Adding new condition for {}", id);
        }
        objState = objState.addCondition(id, condition);
    }

    public void addOnEntry(String id, Runnable onEntryFnc) {
        if (objState.onEntries.containsKey(id)) {
            log.info("Overwriting on-entry function for {}", id);
        } else {
            log.info("Adding on-entry function for {}", id);
        }
        objState = objState.addOnEntry(id, onEntryFnc);
    }

    public void reset() {
        objState = objState.setState(stateChart.getInitialState());
    }

    public String getInitialState() {
        return stateChart.getInitialState();
    }

    public StateChart getStateChart() {
        return stateChart;
    }
}
