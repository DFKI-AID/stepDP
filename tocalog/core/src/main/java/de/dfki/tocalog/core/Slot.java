package de.dfki.tocalog.core;

import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.model.Confidence;
import de.dfki.tocalog.model.Entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 */
public abstract class Slot<T> { // TODO replace T with XYZ<T>
    public String name;
    private List<Candidate<T>> candidates;
    private Map<String, String> annotations;

    public Slot() {
    }


    public List<Candidate<T>> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate<T>> candidates) {
        this.candidates = candidates;
    }

    public void addCandidate(Candidate<T> candidate) {
        candidates.add(candidate);
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


    public abstract Collection<T> findMatches();



    public static class Candidate<T> {
        private T candidate;
        private Confidence confidence;

        public T getCandidate() {
            return candidate;
        }

        public void setCandidate(T candidate) {
            this.candidate = candidate;
        }


        public Confidence getConfidence() {
            return confidence;
        }

        public void setConfidence(Confidence confidence) {
            this.confidence = confidence;
        }
    }

//    public static void main(String[] args) {
//        KnowledgeBase kb;
//        Slot<String> citySlot = new Slot<String>() {
//            @Override
//            public Collection<String> findMatches() {
//                kb.getKnowledgeMap().
//                return Arrays.asList("SB, NYC, sfsd");
//            }
//        };
//
//        DeviceSlot slot = new DeviceSlot(kb) {
//            public Collection<Device> filter(Collection<Device> devices) {
//                //filter
//            }
//        }
//
//        DateSlot, NumberSlot, LocationSlot;
//
//    }
}
