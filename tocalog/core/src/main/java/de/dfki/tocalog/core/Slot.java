package de.dfki.tocalog.core;


import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.kb.Type;

import java.util.*;

/**
 * TODO scheme
 */
public class Slot {
    public final String name;
    private SlotConstraint slotConstraint;
    private Set<Entity> candidates = new HashSet<>();
    private Entity finalSlotEntity;
    private Map<Class, Boolean> matchedMap = new HashMap<>();
    private boolean isOptional = false;


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
        return Optional.ofNullable(slotConstraint);
    }

    public void setSlotConstraint(SlotConstraint slotConstraint) {
        this.slotConstraint = slotConstraint;
    }



    public Optional<Entity> getFinalSlotEntity() {
        return Optional.ofNullable(finalSlotEntity);
    }

    public void setFinalSlotEntity(Entity finalSlotEntity) {
        this.finalSlotEntity = finalSlotEntity;
    }


    public Map<Class, Boolean> getMatchedMap() {
        return matchedMap;
    }

    public void setMatchedMap(Map<Class, Boolean> matchedMap) {
        this.matchedMap = matchedMap;
    }

    public void addMatch(Class clazz, boolean matched) {
        this.matchedMap.put(clazz, matched);
    }


    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
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
        public abstract boolean validateCandidate(Entity candidate);
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
        public boolean validateCandidate(Entity candidate) {
            if(rangeValues.contains(candidate.get(Ontology.name).orElse(""))) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "SlotRangeConstraint{" +
                    "rangeValues=" + rangeValues +
                    '}';
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
        public boolean validateCandidate(Entity candidate) {
            if(Ontology.Number.getName().toLowerCase().contains(candidate.get(Ontology.type).orElse(""))) {
                double value = Double.parseDouble(candidate.get(Ontology.name).orElse("-1"));
                if(startInterval <= value && endInterval >= value) {
                    return true;
                }
            }

            return false;
        }


        @Override
        public String toString() {
            return "NumericSlotConstraint{" +
                    "startInterval=" + startInterval +
                    ", endInterval=" + endInterval +
                    '}';
        }
    }

    public static class SlotTypeConstraint extends SlotConstraint {

        private String type;

        public SlotTypeConstraint(String type) {
            this.type = type;
        }


        @Override
        public boolean validateCandidate(Entity candidate) {
           /* if(!type.getName().toLowerCase().contains(candidate.get(Ontology.type).orElse(""))) {
                return false;
            }*/

            return false;
        }


        public boolean validateType(String slotType) {
            return type.toLowerCase().contains(slotType);
        }

        @Override
        public String toString() {
            return "SlotTypeConstraint{" +
                    "type=" + type +
                    '}';
        }
    }

}
