package de.dfki.tocalog.dialog.sc;

/**
 */
public abstract class Transition {
    private final String id;
    private final State source, target;

    public Transition(String id, State source, State target) {
        this.id = id;
        this.source = source;
        this.target = target;
    }

    public State getSource() {
        return source;
    }

    public State getTarget() {
        return target;
    }

    public String getId() {
        return id;
    }

    public abstract boolean fires(StateChartEvent eve);
}
