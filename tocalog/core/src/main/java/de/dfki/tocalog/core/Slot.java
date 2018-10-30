package de.dfki.tocalog.core;


import de.dfki.tocalog.kb.Entity;

import java.util.Collection;
import java.util.Collections;

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

    private static final class EmptySlot extends Slot {
        protected EmptySlot() {
            super("EMPTY_SLOT");
        }

        @Override
        public Collection<Entity> findCandidates() {
            return Collections.EMPTY_LIST;
        }
    }

    public static final Slot Empty = new EmptySlot();

}
