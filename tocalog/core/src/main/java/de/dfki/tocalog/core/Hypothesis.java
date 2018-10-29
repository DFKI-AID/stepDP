package de.dfki.tocalog.core;

import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.kb.KnowledgeBase;

import java.util.*;

/**
 */
public class Hypothesis {
    private final String id = UUID.randomUUID().toString().substring(0,10);
    private final String intent;
    private final Map<String, Slot> slots;
    private final Set<String> inputs;


    public Hypothesis(Builder builder) {
        this.intent = builder.intent;
        this.slots = Collections.unmodifiableMap(new HashMap<>(builder.slots));
        this.inputs = Collections.unmodifiableSet(new HashSet<>(builder.inputs));
    }

    public String getIntent() {
        return intent;
    }

    public Map<String, Slot> getSlots() {
        return slots;
    }

    public Optional<Slot> getSlot(String slot) {
        return Optional.ofNullable(slots.get(slot));
    }

    public String getId() {
        return id;
    }

    /**
     * @param other
     * @return The number of inputs this Hypothesis was based on, which also part of the other provided Hypothesis
     */
    public int overlaps(Hypothesis other) {
        int count = 0;
        for(String id : inputs) {
            if(other.inputs.contains(id)) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return The inputs this hypothesis is based on.
     */
    public Set<String> getInputs() {
        return inputs;
    }

    interface Query<T extends Ontology.Ent> {
        Collection<T> findMatches(KnowledgeBase kb);
    }

    public static Builder build(String intent) {
        return new Builder(intent);
    }

    public static class Builder {
        private String intent;
        private Map<String, Slot> slots = new HashMap<>();
        private Set<String> inputs = new HashSet<>();

        public Builder(String intent) {
            this.intent = intent;
        }

//        public <T extends Entity> Builder addSlot(String name, Query<T> query) {
//            return addSlot(name, new Slot() {
//                @Override
//                public Collection<? extends Entity> findMatches(KnowledgeBase kb) {
//                    return query.findMatches(kb);
//                }
//            });
//        }

        public <T extends Ontology.Ent> Builder addSlot(String name, Slot<T> slot) {
            slots.put(name, slot);
            slots.get(name).name = name;
            return this;
        }

        public Hypothesis build() {
            return new Hypothesis(this);
        }

        /**
         * Add an Input that was used to derive the hypothesis
         * @param input
         */
        public void addInput(Input input) {
            inputs.add(input.getId());
        }
    }
}
