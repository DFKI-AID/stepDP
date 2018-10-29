package de.dfki.tocalog.core;


import java.util.Collection;

/**
 */
public abstract class Slot<T> { // TODO replace T with XYZ<T>
    public String name;

    public Slot() {
    }

    public String getName() {
        return name;
    }

    public abstract Collection<T> findMatches();

    public static class Candidate<T> {
        private T candidate;
        private Confidence confidence;
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
