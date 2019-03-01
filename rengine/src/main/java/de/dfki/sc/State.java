package de.dfki.sc;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public class State {
    private final String id;
    private PSequence<State> children = TreePVector.empty();
    private PSequence<Transition> transitions = TreePVector.empty();
    private PSequence<OnEntry> onEntries = TreePVector.empty();
    private Geometry geometry;

    public State(String id) {
        this.id = id;
    }

    protected void addChildState(State state) {
        children = children.plus(state);
    }

    protected void addTransition(Transition transition) {
        transitions = transitions.plus(transition);
    }

    protected void addOnEntry(OnEntry onEntry) {
        onEntries = onEntries.plus(onEntry);
    }

    public Optional<Geometry> getGeometry() {
        return Optional.ofNullable(geometry);
    }

    protected void setGeometry(Geometry geometry) {
        this.geometry = geometry;
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

    public PSequence<OnEntry> getOnEntries() {
        return onEntries;
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
