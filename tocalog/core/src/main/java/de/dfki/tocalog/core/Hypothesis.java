package de.dfki.tocalog.core;

import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;

import java.util.*;

/**
 */
public class Hypothesis {
    private final String id = UUID.randomUUID().toString().substring(0, 10);
    private final String intent;
    private final Map<String, Slot> slots;
    //warum string und nicht input?
    private final Set<String> inputs;
    private final Confidence confidence;
    private Map<Class, Boolean> matchedMap = new HashMap<>();
   // private boolean isFilled = false;

    public Map<Class, Boolean> getMatchedMap() {
        return matchedMap;
    }

    public void setMatchedMap(Map<Class, Boolean> matchedMap) {
        this.matchedMap = matchedMap;
    }

    public void addMatch(Class clazz, boolean matched) {
        this.matchedMap.put(clazz, matched);
    }


    /* public boolean isFilled() {
        return isFilled;
    }

    public void setFilled(boolean filled) {
        isFilled = filled;
    }

    public boolean isFilledConfident(double threshold) {
        if(confidence.getConfidence() > threshold) {
            return true;
        }
        return false;
    }*/




    public Hypothesis(Builder builder) {
        this.intent = builder.intent;
        this.slots = Collections.unmodifiableMap(new HashMap<>(builder.slots));
        this.inputs = Collections.unmodifiableSet(new HashSet<>(builder.inputs));
        this.confidence = builder.confidence;
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

    public Confidence getConfidence() {
        return confidence;
    }

    /**
     * @param other
     * @return The number of inputs this Hypothesis was based on, which also part of the other provided Hypothesis
     */
    public int overlaps(Hypothesis other) {
        int count = 0;
        for (String id : inputs) {
            if (other.inputs.contains(id)) {
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


    interface Query<T extends Entity> {
        Collection<T> findMatches(KnowledgeBase kb);
    }

    public static Builder create(String intent) {
        return new Builder(intent);
    }


    @Override
    public String toString() {
        return "Hypothesis{" +
                "id='" + id + '\'' +
                ", intent='" + intent + '\'' +
                ", slots=" + slots +
                ", inputs=" + inputs +
                ", confidence=" + confidence +
                '}';
    }

    public static class Builder {
        private String intent;
        private Map<String, Slot> slots = new HashMap<>();
        private Set<String> inputs = new HashSet<>();
        private Confidence confidence = Confidence.UNKNOWN;

        public Builder(String intent) {
            this.intent = intent;
        }

        public Builder(Hypothesis otherHypothesis) {
            this.intent = otherHypothesis.intent;
            this.slots = otherHypothesis.slots;
            this.confidence = otherHypothesis.confidence;
            this.inputs = otherHypothesis.inputs;
        }

        public Builder setConfidence(Confidence confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder addSlot(Slot slot) {
            if(slots.containsKey(slot.getName())) {
                throw new IllegalArgumentException("contains already a slot with the name: " + slot.getName());
            }
            slots.put(slot.getName(), slot);
            return this;
        }

        public Hypothesis build() {
            return new Hypothesis(this);
        }

        /**
         * Add an Input that was used to derive the hypothesis
         *
         * @param input
         */
        public Builder addInput(Input input) {
            inputs.add(input.getId());
            return this;
        }
    }
}
