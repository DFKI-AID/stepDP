package de.dfki.tocalog.core;


import de.dfki.tocalog.kb.Entity;

import java.util.Collection;

/**
 */
public abstract class Slot {
    public final String name;

    protected Slot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract Collection<Entity> findCandidates();

}
