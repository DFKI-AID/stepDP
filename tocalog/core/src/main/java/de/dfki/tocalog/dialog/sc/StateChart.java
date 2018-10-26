package de.dfki.tocalog.dialog.sc;

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
    private final String initialState;
    private final Set<String> states;
    private final Set<Transition> transitions;
    private final Callback callback;
    private String currentState;

    interface Callback {
        void fires(String s1, Transition t, String s2);
    }

    public static Builder create() {
        return new Builder();
    }


    /**
     * @param eve
     * @return true iff the name was consumed
     */
    public boolean update(String eve) {
        Set<Transition> transitions = getTransitionCandidates(currentState);
        for (Transition transition : transitions) {
            //TODO selection strategy
            if (transition.getCond().equals(eve)) {
                fireTransition(transition);
                return true;
            }
        }
        return false;
    }

    public boolean canUpdate(String eve) {
        Set<Transition> transitions = getTransitionCandidates(currentState);
        for (Transition transition : transitions) {
            if (transition.getCond().equals(eve)) {
                return true;
            }
        }
        return false;
    }


    public String toMermaid() {
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD\n");
        //TODO escape state names
        sb.append(String.format("%s((%s)) --> %s(%s)\n",
                "init", "init", initialState, initialState));
        this.transitions.forEach(t -> {
            sb.append(String.format("%s --> |%s| %s(%s)\n",
                    t.getSource(), t.getCond(), t.getTarget(), t.getTarget()));
        });
        return sb.toString();
    }


    private StateChart(Builder builder) {
        this.states = Collections.unmodifiableSet(builder.states);
        this.transitions = Collections.unmodifiableSet(builder.transitions);
        this.initialState = builder.initialState;
        this.currentState = initialState;
        this.callback = builder.callback;
    }


    protected void fireTransition(Transition transition) {
        String srcState = transition.getSource();
        String targetState = transition.getTarget();
        log.info("transition fired: {} -{}-> {}", srcState, transition.getCond(), targetState);
        if (!states.contains(targetState)) {
            throw new IllegalArgumentException();
        }
        if (!srcState.equals(currentState)) {
            throw new IllegalStateException(String.format("can't fire transition %s. not in %s", transition, srcState));
        }
        this.currentState = targetState;
        callback.fires(srcState, transition, targetState);
    }

    /**
     * transitions from the given state
     *
     * @param state
     * @return
     */
    private Set<Transition> getTransitionCandidates(String state) {
        return transitions.stream()
                .filter(t -> t.getSource().equals(state))
                .collect(Collectors.toSet());
    }


    public static class Builder {
        private Set<String> states;
        private Set<Transition> transitions = new HashSet<>();
        private String initialState;
        private Callback callback;

        public Builder addTransition(Transition transition) {
            transitions.add(transition);
            if(initialState == null) {
                initialState = transition.getSource();
            }
            return this;
        }

        public Builder setInitialState(String initialState) {
            this.initialState = initialState;
            return this;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public StateChart build() {
            if (callback == null) {
                callback = (s1, t, s2) -> log.info("state change {} -{}-> {}", s1, t.getCond(), s2);
            }
            states = new HashSet<String>();
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

    public static void main(String[] args) {
//        Callback c = (s1, t, s2) -> {
//
//        };
        StateChart sc = StateChart.create()
                .addTransition(new Transition("init", "asks_for_tool", "looking_for_tool"))
                .addTransition(new Transition("looking_for_tool", "found_tool", "exit"))
                .build();

        sc.update("asks_for_tool");
        sc.update("asks_for_tool");
        sc.update("found_tool");
    }
}
