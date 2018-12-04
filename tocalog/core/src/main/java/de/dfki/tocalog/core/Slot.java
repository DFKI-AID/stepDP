package de.dfki.tocalog.core;


import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.kb.Type;

import java.util.*;

/**
 * TODO scheme
 */
public class Slot {
    public final String name;
    private Optional<SlotConstraint> slotConstraint;
    private Set<Entity> candidates = new HashSet<>();



    public Slot(String name) {
        this.name = name;
    }


    public Collection<Entity> getCandidates() {
        return Collections.unmodifiableSet(candidates);
    }

    public void setCandidates(Collection<Entity> candidates) {
        this.candidates.addAll(candidates);

    }

    public  void addCandidate(Entity entity) {
        candidates.add(entity);
    }

    public Optional<SlotConstraint> getSlotConstraint() {
        return slotConstraint;
    }

    public void setSlotConstraint(SlotConstraint slotConstraint) {
        this.slotConstraint = Optional.of(slotConstraint);
    }



    @Override
    public String toString() {
        return "Slot{" +
                "name='" + name + '\'' +
                "slotConstraint='" + slotConstraint + '\'' +
                ", candidates=" + candidates.toString() +
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



    public static abstract class SlotConstraint {
        public abstract Object getConstraint();
        public abstract boolean validateCandidate(Entity candidate);
        public abstract boolean validateType(String slotType);
    }

    public static class SlotRangeConstraint extends SlotConstraint {

        private List<String> rangeValues;

        public SlotRangeConstraint(List<String> rangeVals) {
            rangeValues = rangeVals;
        }

        public SlotRangeConstraint() {
            rangeValues = Collections.EMPTY_LIST;
        }

        @Override
        public Object getConstraint() {
            return rangeValues;
        }

        @Override
        public boolean validateCandidate(Entity candidate) {
            if(rangeValues.contains(candidate.get(Ontology.name).orElse(""))) {
                return true;
            }
            return false;
        }

        @Override
        public boolean validateType(String slotType) {
            return false;
        }
    }


    public static class NumericSlotConstraint extends SlotConstraint {

        private double startInterval;
        private double endInterval;


        public NumericSlotConstraint(double startInterval, double endInterval) {
            this.startInterval = startInterval;
            this.endInterval = endInterval;
        }

        @Override
        public Object getConstraint() {
            return List.of(startInterval, endInterval);
        }

        @Override
        public boolean validateCandidate(Entity candidate) {
            double value = Double.parseDouble(candidate.get(Ontology.name).orElse("-1"));
            if(candidate.get(Ontology.type).orElse("").equals(Ontology.Numeric.getName())
                    && startInterval <= value && endInterval >= value) {
                return true;
            }
            return false;
        }

        @Override
        public boolean validateType(String slotType) {
            return false;
        }
    }

    public static class SlotTypeConstraint extends SlotConstraint {

        private Type type;

        public SlotTypeConstraint(Type type) {
            this.type = type;
        }

        @Override
        public Object getConstraint() {
            return type;
        }

        @Override
        public boolean validateCandidate(Entity candidate) {
            if(candidate.get(Ontology.type).orElse("").equals(type.getName())) {
                return true;
            }
            return false;
        }

        @Override
        public boolean validateType(String slotType) {
            return type.getName().equals(slotType);
        }
    }

}
