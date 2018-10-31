package de.dfki.tocalog.dialog.sc;

/**
 */
public abstract class Transition<T> {
    private final String source, target;

    public Transition(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public abstract boolean canFire(T event);
}
