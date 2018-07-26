package de.dfki.tocalog.core.sc;

import de.dfki.tocalog.core.Event;

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

    public abstract boolean fires(Event event);

    public interface Iface {
        boolean fires(Event event);
    }

    public static Transition create(String id, State source, State target, Iface fireFnc) {
        return new Transition(id, source, target) {
            @Override
            public boolean fires(Event event) {
                return fireFnc.fires(event);
            }
        };
    }
}
