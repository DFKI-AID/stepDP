package de.dfki.tocalog.dialog.sc;

import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.framework.Event;
import de.dfki.tocalog.framework.EventEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
public class StateChart {
    private static Logger log = LoggerFactory.getLogger(StateChart.class);
    private final State initialState;
    private final Set<State> states;
    private final Set<Transition> transitions;
    private State currentState;

    private StateChart(Builder builder) {
        this.states = Collections.unmodifiableSet(builder.states);
        this.transitions = Collections.unmodifiableSet(builder.transitions);
        this.initialState = builder.initialState;
        this.currentState = initialState;
    }


    protected void fireTransition(Transition transition, StateChartEvent eve) {
        log.info("transition fired: {} -{}-> {}", currentState.getId(), transition.getId(), transition.getTarget().getId());
        setCurrentState(transition.getTarget());
    }

    public String toMermaid() {
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD\n");
        //TODO escape state names
        sb.append(String.format("%s((%s)) --> %s(%s)\n",
                "init", "init", initialState.getId(), initialState.getId()));
        this.transitions.forEach(t -> {
            sb.append(String.format("%s --> |%s| %s(%s)\n",
                    t.getSource().getId(), t.getId(), t.getTarget().getId(), t.getTarget().getId()));
        });
        return sb.toString();
    }

    protected void setCurrentState(State state) {
        if(!states.contains(state)) {
            throw new IllegalArgumentException();
        }
        log.debug("state change {} -> {}", currentState.getId(), state.getId());
        currentState.onExit();
        currentState = state;
        currentState.onEntry();
    }

    /**
     * transitions from the given state
     * @param state
     * @return
     */
    private Set<Transition> getTransitionCandidates(State state) {
        return transitions.stream()
                .filter(t -> t.getSource().equals(state))
                .collect(Collectors.toSet());
    }

    public static Builder create() {
        return new Builder();
    }

    /**
     *
     * @param eve
     * @return true iff the intent was consumed
     */
    public boolean onEvent(StateChartEvent eve) {
        Set<Transition> transitions = getTransitionCandidates(currentState);
        for(Transition transition : transitions) {
            //TODO selection strategy
            if (transition.fires(eve)) {
                fireTransition(transition, eve);
                return true;
            }
        }
        return false;
    }

    public static class Builder {
        private Set<State> states;
        private Set<Transition> transitions = new HashSet<>();
        private State initialState;

        public Builder addTransition(Transition transition) {
            transitions.add(transition);
            return this;
        }

        public Builder setInitialState(State initialState) {
            this.initialState = initialState;
            return this;
        }

        public StateChart build() {
            states = new HashSet<State>();
            transitions.forEach(t -> {
                states.add(t.getSource());
                states.add(t.getTarget());
            });
            return new StateChart(this);
        }

        private void verify() {
            //TODO check for disjunct names of states
        }
    }
}
