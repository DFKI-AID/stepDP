package de.dfki.tocalog.dialog.sc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
public class StateChart<T> {
    private static Logger log = LoggerFactory.getLogger(StateChart.class);
    private final String initialState;
    private final Set<String> states;
    private final Set<Transition> transitions;
    private final Callback callback;
    private String currentState;

    public static class FireEvent<T> {
        public final String source;
        public final String target;
        public final Transition<T> transition;
        public final T event;

        public FireEvent(String source, String target, Transition<T> transition, T event) {
            this.source = source;
            this.target = target;
            this.transition = transition;
            this.event = event;
        }
    }

    public interface Callback<T> {
        void onFire(FireEvent<T> event);
    }

    public static <T> Builder<T> create() {
        return new Builder<>();
    }


    /**
     * @param event
     * @return true iff the name was consumed
     */
    public boolean fire(T event) {
        Set<Transition> transitions = getTransitionCandidates(currentState);
        for (Transition transition : transitions) {
            //TODO selection strategy
            if (transition.canFire(event)) {
                fireTransition(transition, event);
                return true;
            }
        }
        return false;
    }

    public boolean canFire(T event) {
        Set<Transition> transitions = getTransitionCandidates(currentState);
        for (Transition transition : transitions) {
            if (transition.canFire(event)) {
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
                    t.getSource(), "todo", t.getTarget(), t.getTarget()));
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


    protected void fireTransition(Transition transition, T event) {
        String srcState = transition.getSource();
        String targetState = transition.getTarget();
        log.info("transition fired: {} --> {}", srcState, targetState);
        if (!states.contains(targetState)) {
            throw new IllegalArgumentException();
        }
        if (!srcState.equals(currentState)) {
            throw new IllegalStateException(String.format("can't fire transition %s. not in %s", transition, srcState));
        }
        this.currentState = targetState;
        callback.onFire(new FireEvent<>(srcState, targetState, transition, event));
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


    public static class Builder<T> {
        private Set<String> states;
        private Set<Transition<T>> transitions = new HashSet<>();
        private String initialState;
        private Callback callback;

        public interface TransitionImpl<T> {
            boolean canFire(T event);
        }

        public Builder<T> addTransition(Transition<T> transition) {
            transitions.add(transition);
            if (initialState == null) {
                initialState = transition.getSource();
            }
            return this;
        }

        public Builder<T> addTransition(String source, String target, TransitionImpl<T> transitionImpl) {
            return this.addTransition(new Transition<>(source, target) {
                @Override
                public boolean canFire(T event) {
                    return transitionImpl.canFire(event);
                }
            });
        }

        public Builder<T> setInitialState(String initialState) {
            this.initialState = initialState;
            return this;
        }

        public Builder<T> setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public StateChart<T> build() {
            if (callback == null) {
                callback = (eve) -> {}; //log.info("state change {} -{}-> {}", s1, t.getCond(), s2);
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

}
