package de.dfki.tocalog.core;


import de.dfki.tocalog.kb.Entity;

import java.util.*;

/**
 * TODO scheme
 */
public class Slot {
    public final String name;
    private Map<String, String> annotations; //TODO maybe replace with Base / Entity class
    private Set<Entity> candidates = new HashSet<>();

    public Slot(String name) {
        this.name = name;
    }

    public Map<String,String> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(String key, String value) {
        annotations.put(key,value);
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public Collection<Entity> getCandidates() {
        return Collections.unmodifiableSet(candidates);
    }

    public void setCandidates(Collection<Entity> candidates) {
        this.candidates = new HashSet<>();
        this.candidates.addAll(candidates);
    }

    @Override
    public String toString() {
        return "Slot{" +
                "name='" + name + '\'' +
                ", annotations=" + annotations +
                ", candidates=" + candidates +
                '}';
    }

    private static final class EmptySlot extends Slot {
        protected EmptySlot() {
            super("EMPTY_SLOT");
        }

        @Override
        public Collection<Entity> getCandidates() {
            return Collections.EMPTY_LIST;
        }
    }

    public String getName() {
        return name;
    }

    public static final Slot Empty = new EmptySlot();

}
