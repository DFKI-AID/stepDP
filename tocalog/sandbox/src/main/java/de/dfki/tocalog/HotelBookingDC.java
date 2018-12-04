package de.dfki.tocalog;

import a.Slot;
import de.dfki.tocalog.core.*;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.output.Imp;
import de.dfki.tocalog.output.SpeechOutput;
import de.dfki.tocalog.output.impp.Allocation;
import de.dfki.tocalog.output.impp.OutputNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 */
public class HotelBookingDC implements DialogComponent {
    private final Imp imp;
    private final KnowledgeBase kb;
    //    private final Resolution speakerResolution;
//    private final Resolution
    private long lastActive = 0;
    private boolean active = false;
    private Slot slot1, slot2;

    public HotelBookingDC(Imp imp, KnowledgeBase kb) {
        this.imp = imp;
        this.kb = kb;
    }

    protected Optional<DialogFunction> onTimeout(Object timeout) {
        throw new RuntimeException("not impl");
    }

    protected Optional<DialogFunction> onTextInput(TextInput textInput) {
        // TODO interface for grammar-based and intent-based recognizer
        // grammer.match(textInput.getGestureType())


//        if (!active && textInput.getText().matches("i want to book a hotel")) {
//            return Optional.of(new AbsDialogFunction(HotelBookingDC.this, textInput) {
//                @Override
//                public void run() {
//                    active = true;
//                    lastActive = System.currentTimeMillis();
//                    Allocation id = imp.allocate(OutputNode.buildNode(new SpeechOutput("ok, let's do this")).build());
//                    List<Slot.Entity> slotCandidates = getSlotCandidates(textInput);
//                    slotCandidates.forEach(sc -> sc.getSlot().consume(textInput));
//                }
//            });
//        }

//        super.requestYesNo("...");


        List<Slot.Entity> slotCandidates = getSlotCandidates(textInput);
        return Optional.of(new AbsDialogFunction(HotelBookingDC.this, textInput) {
            @Override
            public void run() {
                lastActive = System.currentTimeMillis();
                slotCandidates.forEach(sc -> sc.getSlot().consume(textInput));
            }
        });


//
//        if (s1p.distance(s2p).getConfidence() < 0.2) {
//            lastActive = System.currentTimeMillis();
//            return Optional.of(new DialogFunction() {
//                @Override
//                public Collection<Input> consumedInputs() {
//                    return List.of(textInput);
//                }
//
//                @Override
//                public Object getOrigin() {
//                    return null;
//                }
//
//                @Override
//                public void run() {
//                    Allocation id = imp.allocate(OutputNode.buildNode(new SpeechOutput("Did you mean <slot1> or <slot2>")).build());
//
//                }
//            });
//        }
//
//

    }

    protected List<Slot.Entity> getSlotCandidates(Input input) {
        List<Slot.Entity> slotCandidates = filterSlots(Set.of(slot1, slot2), input);
        Set<Object> consumedObject = new HashSet<>();
        Iterator<Slot.Entity> iter = slotCandidates.iterator();
        while (iter.hasNext()) {
            Slot.Entity se = iter.next();
            if (consumedObject.contains(se.getObject())) {
                iter.remove();
                continue;
            }
            consumedObject.add(se.getObject());
        }
        return slotCandidates;
    }

    protected List<Slot.Entity> filterSlots(Collection<Slot> slots, Input input) {
        return slots.stream()
                .filter(s -> !s.isFilled())
                .map(s -> s.consumes(input))
                .filter(e -> e.getConfidence().getConfidence() > 0.8)
                .sorted(Comparator.comparing(Slot.Entity::getConfidence))//TODO maybe swap order
                .collect(Collectors.toList());
    }


    protected DialogFunction createDialogFunction(Runnable run, Input input) {
        return new DialogFunction() {
            @Override
            public Collection<Input> consumedInputs() {
                return List.of(input);
            }

            @Override
            public Object getOrigin() {
                return HotelBookingDC.this;
            }

            @Override
            public void run() {
                run.run();
            }
        };
    }

    abstract class AbsDialogFunction implements DialogFunction {
        private Object origin = HotelBookingDC.this;
        private Collection<Input> consumedInputs;

        public AbsDialogFunction(Object origin, Collection<Input> consumedInputs) {
            this.origin = origin;
            this.consumedInputs = consumedInputs;
        }


        public AbsDialogFunction(Object origin, Input... consumedInputs) {
            this.origin = origin;
            this.consumedInputs = List.of(consumedInputs);
        }

        @Override
        public Collection<Input> consumedInputs() {
            return consumedInputs;
        }

        @Override
        public Object getOrigin() {
            return origin;
        }
    }


    protected double getConfidence(long timestamp) {
        long elapsed = Math.max(System.currentTimeMillis() - timestamp, 0);
        return elapsed > 10000 ? 0.0 : 1.0;
    }

    @Override
    public Optional<DialogFunction> process(Inputs inputs) {
        long elapsed = Math.max(System.currentTimeMillis() - lastActive, 0);
        if (elapsed > 10000) {
            active = false;
        }

        for (Input input : inputs.getInputs()) {
            if (inputs.isConsumed(input)) {
                continue;
            }

            if (input instanceof TextInput) {
                Optional<DialogFunction> df = onTextInput((TextInput) input);
                if (df.isPresent()) {
                    return df;
                }
            }
        }

        return Optional.empty();
    }
}
