package de.dfki.sc;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.List;

/**
 *
 */
public class State {
    private final String id;
    private PSequence<State> children = TreePVector.empty();
    private PSequence<Transition> transitions = TreePVector.empty();

    public State(String id) {
        this.id = id;
    }

    protected void addChildState(State state) {
        children = children.plus(state);
    }

    protected void addTransition(Transition transition) {
        transitions = transitions.plus(transition);
    }

    public String getId() {
        return id;
    }

    public List<State> getChildren() {
        return children;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    @Override
    public String toString() {
        return "State{" +
                "id='" + id + '\'' +
                ", children=" + children.stream().map(s -> s.toString())
                .reduce("", (s1, s2) -> s1 + " " + s2) +
                '}';
    }
}
