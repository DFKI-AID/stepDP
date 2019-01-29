package de.dfki.tocalog.examples.device_control;


import de.dfki.tocalog.core.HypothesisProcessor;
import de.dfki.tocalog.core.*;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.output.Imp;
import de.dfki.tocalog.output.SpeechOutput;
import de.dfki.tocalog.output.impp.Allocation;
import de.dfki.tocalog.output.impp.OutputNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeviceControlDC implements DialogComponent {
    private List<Hypothesis> deviceControlHypos = new ArrayList<>();
    private List<HypothesisProcessor> processors = new ArrayList<>();
    private Map<Hypothesis, Double> intentCountMap = new HashMap<>();
    private final Imp imp;
    private long lastActive = 0;



    private final double THRESHOLD = 0.1;

    public DeviceControlDC( List<HypothesisProcessor> processors, Imp imp) {
        this.processors = processors;
        this.imp = imp;
        createDeviceControlHypos();
    }




    public void createDeviceControlHypos() {
        //Turn on _DEV
        Hypothesis.Builder turnOnBuilder = new Hypothesis.Builder("turnOn");
        Slot slot = new Slot("dev");
        slot.setSlotConstraint(new Slot.SlotTypeConstraint(Ontology.Device));
        turnOnBuilder.addSlot(slot);
        deviceControlHypos.add(turnOnBuilder.build());

        //Turn off _DEV
        Hypothesis.Builder turnOffBuilder = new Hypothesis.Builder("turnOff");
        slot = new Slot("dev");
        slot.setSlotConstraint(new Slot.SlotTypeConstraint(Ontology.Device));
        turnOffBuilder.addSlot(slot);
        deviceControlHypos.add(turnOffBuilder.build());

        //Change_color _DEV _COLOR
        Hypothesis.Builder changeColorBuilder = new Hypothesis.Builder("changeColor");
        slot = new Slot("dev");
        slot.setSlotConstraint(new Slot.SlotTypeConstraint(Ontology.Device));
        Slot slot2 = new Slot("color");
        slot2.setSlotConstraint(new Slot.SlotRangeConstraint(List.of("white", "blue", "green", "red", "yellow")));
        changeColorBuilder.addSlot(slot);
        changeColorBuilder.addSlot(slot2);
        deviceControlHypos.add(changeColorBuilder.build());

        //Increase_brightness _DEV _Brightness
        Hypothesis.Builder increaseBrightnessBuilder = new Hypothesis.Builder("increaseBrightness");
        slot = new Slot("dev");
        slot.setSlotConstraint(new Slot.SlotTypeConstraint(Ontology.Device));
        slot2 = new Slot("brightness");
        slot2.setSlotConstraint(new Slot.NumericSlotConstraint(0.0, 360.0));
        increaseBrightnessBuilder.addSlot(slot);
        increaseBrightnessBuilder.addSlot(slot2);
        deviceControlHypos.add(increaseBrightnessBuilder.build());

        //Decrease_brightness _DEV _Brightness
        Hypothesis.Builder decreaseBrightnessBuilder = new Hypothesis.Builder("decreaseBrightness");
        slot = new Slot("dev");
        slot.setSlotConstraint(new Slot.SlotTypeConstraint(Ontology.Device));
        slot2 = new Slot("brightness");
        slot2.setSlotConstraint(new Slot.NumericSlotConstraint(0.0, 360.0));
        decreaseBrightnessBuilder.addSlot(slot);
        decreaseBrightnessBuilder.addSlot(slot2);
        deviceControlHypos.add(decreaseBrightnessBuilder.build());

    }


    @Override
    public Optional<DialogFunction> process(Inputs inputs) {
        Input currentInput = inputs.getInputs().get(inputs.getInputs().size()-1);
        for(Hypothesis hypo: deviceControlHypos) {
            for (HypothesisProcessor processor : processors) {
                processor.process(currentInput, hypo);
            }
        }

        Hypothesis matchedHypo = getMostRelevantHypo(deviceControlHypos);

        if(matchedHypo == null) {
            return Optional.empty();
        }
        System.out.println("matchedHypo: " + matchedHypo.toString());
        for(Slot slot: matchedHypo.getSlots().values()) {

            OptionalDouble max = slot.getCandidates().stream().mapToDouble(v -> v.get(Ontology.confidence).get()).max();
            if (max.isPresent()) {
                Collection<Entity> slotEntities = slot.getCandidates().stream().filter(e -> e.get(Ontology.confidence).get().equals(max.getAsDouble())).collect(Collectors.toList());
                if (slotEntities.size() >= 2) {
                    System.out.println("Do you mean ");
                    for (int i = 0; i < slotEntities.size(); i++) {
                        if (i == slotEntities.size() - 1) {
                            System.out.println(((List<Entity>) slotEntities).get(i).get(Ontology.name).get());
                        } else {
                            System.out.println(((List<Entity>) slotEntities).get(i).get(Ontology.name).get() + " or ");
                        }
                    }
                    return Optional.empty();
                } else {
                    slot.setFinalSlotEntity(((List<Entity>) slotEntities).get(0));
                }
            }
        }
        for(Slot slot: matchedHypo.getSlots().values()) {
            if(!slot.isOptional() && slot.getCandidates().isEmpty()) {
                //TODO: wait short time and re-check if slot value is provided, for now: directly ask for value
                if(matchedHypo.getIntent().equals("turnOn")) {
                    System.out.println("Which device should I turn on?");
                } else if(matchedHypo.getIntent().equals("turnOff")) {
                    System.out.println("Which device should I turn off?");
                } else if(matchedHypo.getIntent().equals("changeColor")) {
                    if (slot.getName().equals("dev")) {
                        System.out.println("For which device the color should be changed?");
                    } else if (slot.getName().equals("color")){
                        System.out.println("Which color should be used?");
                    }
                }else if(matchedHypo.getIntent().equals("increaseBrightness")) {
                    if (slot.getName().equals("dev")) {
                        System.out.println("For which device the brightness should be increased?");
                        //increase brightness +10 default
                    } else if (slot.getName().equals("brightness")){
                        Slot devSlot = matchedHypo.getSlots().values().stream().filter(s -> s.getName().equals("dev")).findAny().get();
                        if(devSlot.getFinalSlotEntity().isPresent()) {
                            double newBrightness = devSlot.getFinalSlotEntity().get().get(Ontology.brightness).get() + 10.0;
                            if(newBrightness > 360) {
                                System.out.println("The brightness cannot be increased any more");
                            }else {
                                slot.setFinalSlotEntity(new Entity().set(Ontology.name, newBrightness + "").set(Ontology.id, newBrightness + "").set(Ontology.type, "number"));
                            }
                        }
                    }
                }else if(matchedHypo.getIntent().equals("decreaseBrightness")) {
                    if (slot.getName().equals("dev")) {
                        System.out.println("For which device the brightness should be decreased?");
                    } else if (slot.getName().equals("brightness")){
                        Slot devSlot = matchedHypo.getSlots().values().stream().filter(s -> s.getName().equals("dev")).findAny().get();
                        if(devSlot.getFinalSlotEntity().isPresent()) {
                            double newBrightness = devSlot.getFinalSlotEntity().get().get(Ontology.brightness).get()- 10.0;
                            if(newBrightness <0 ) {
                                System.out.println("The brightness cannot be decreased any more");
                            }else {
                                slot.setFinalSlotEntity(new Entity().set(Ontology.name, newBrightness + "").set(Ontology.id, newBrightness + "").set(Ontology.type, "number"));
                            }
                        }
                    }
                }
            }


        }
        if(!matchedHypo.getSlots().values().stream().allMatch(s -> s.getFinalSlotEntity().isPresent())) {
            return Optional.empty();
        }

        //debug
        if(matchedHypo.getIntent().equals("turnOn")) {
            System.out.println(("ok, I will turn on the  " + matchedHypo.getSlots().get("dev").getFinalSlotEntity().get().get(Ontology.name).get()));
        } else if(matchedHypo.getIntent().equals("turnOff")) {
            System.out.println(("ok, I will turn off the  " + matchedHypo.getSlots().get("dev").getFinalSlotEntity().get().get(Ontology.name).get()));
        } else if(matchedHypo.getIntent().equals("changeColor")) {
            System.out.println(("ok, I will change the  color of " + matchedHypo.getSlots().get("dev").getFinalSlotEntity().get().get(Ontology.name).get() + " to " + matchedHypo.getSlots().get("color").getFinalSlotEntity().get().get(Ontology.name).get()));
        }else if(matchedHypo.getIntent().equals("increaseBrightness")) {
            System.out.println(("ok, I will increase the brightness of  " + matchedHypo.getSlots().get("dev").getFinalSlotEntity().get().get(Ontology.name).get() +  " to " + matchedHypo.getSlots().get("brightness").getFinalSlotEntity().get().get(Ontology.name).get()));
        }else if(matchedHypo.getIntent().equals("decreaseBrightness")) {
            System.out.println("ok, I will decrease the brightness of  " + matchedHypo.getSlots().get("dev").getFinalSlotEntity().get().get(Ontology.name).get() +  " to " + matchedHypo.getSlots().get("brightness").getFinalSlotEntity().get().get(Ontology.name).get());
        }

        return Optional.of(new DeviceControlDC.AbsDialogFunction(DeviceControlDC.this, currentInput) {
            @Override
            public void run() {
                lastActive = System.currentTimeMillis();
                Allocation id = null;
                if(matchedHypo.getIntent().equals("turnOn")) {
                    id = imp.allocate(OutputNode.buildNode(new SpeechOutput("ok, I will turn on the  " + matchedHypo.getSlots().get("dev").getFinalSlotEntity().get().get(Ontology.id).get())).build());
                } else if(matchedHypo.getIntent().equals("turnOff")) {
                    id = imp.allocate(OutputNode.buildNode(new SpeechOutput("ok, I will turn off the  " + matchedHypo.getSlots().get("dev").getFinalSlotEntity().get().get(Ontology.id).get())).build());
                } else if(matchedHypo.getIntent().equals("changeColor")) {
                    id = imp.allocate(OutputNode.buildNode(new SpeechOutput("ok, I will change the  color of " + matchedHypo.getSlots().get("dev").getFinalSlotEntity().get().get(Ontology.id).get() + " to " + matchedHypo.getSlots().get("color").getFinalSlotEntity().get().get(Ontology.id).get())).build());
                }else if(matchedHypo.getIntent().equals("increaseBrightness")) {
                    id = imp.allocate(OutputNode.buildNode(new SpeechOutput("ok, I will increase the brightness of  " + matchedHypo.getSlots().get("dev").getFinalSlotEntity().get().get(Ontology.id).get() +  " to " + matchedHypo.getSlots().get("brightness").getFinalSlotEntity().get().get(Ontology.id).get())).build());
                }else if(matchedHypo.getIntent().equals("decreaseBrightness")) {
                    id = imp.allocate(OutputNode.buildNode(new SpeechOutput("ok, I will decrease the brightness of  " + matchedHypo.getSlots().get("dev").getFinalSlotEntity().get().get(Ontology.id).get() +  " to " + matchedHypo.getSlots().get("brightness").getFinalSlotEntity().get().get(Ontology.id).get())).build());
                }

            }
        });


        //check if for current input all processors match same hypo intent

        //check slot candidates: same resolved entities for several processors, i.e. if different and confidence for both high -> clarification question

        //if not all slots filled: give prefilled hypo to processor so that they can add slot values...


    }

    public Hypothesis getMostRelevantHypo(List<Hypothesis> hypotheses) {
        double intentThreshold = 0.5;
        Hypothesis resultHypo = null;
        double prevval = -1;
        for(Hypothesis hypo: hypotheses) {
            //check if at least one match has a confidence higher than threshold
            if(hypo.getMatches().values().stream().filter(e -> e.getConfidence() > intentThreshold).collect(Collectors.toList()).isEmpty()) {
                continue;
            }
            double currentval = hypo.getMatches().values().stream().mapToDouble(i -> i.getConfidence()).sum();
            if(prevval == -1) {
                resultHypo = hypo;
                prevval = currentval;
            }
            if(currentval > prevval) {
                resultHypo = hypo;
            }
        }
        return resultHypo;
    }

    public List<Hypothesis> getHyposWithRelevantIntent(List<Hypothesis> hypotheses) {
        for(Hypothesis hypo: hypotheses) {
            intentCountMap.put(hypo, hypo.getMatches().values().stream().mapToDouble(i -> i.getConfidence()).sum());
        }
        Map<Hypothesis,Double> hypoIntentRelevanceMap =
                intentCountMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<Hypothesis> relevantIntentHypos = new ArrayList<>();
        boolean first = true;
        double topValue = 0.0;
        for(Hypothesis hypo: hypoIntentRelevanceMap.keySet()) {
            if(first) {
                topValue = hypoIntentRelevanceMap.get(hypo);
                relevantIntentHypos.add(hypo);
                first = false;
            }
            if(Math.abs(topValue - hypoIntentRelevanceMap.get(hypo)) <= THRESHOLD) {
                relevantIntentHypos.add(hypo);
            }

        }

        return relevantIntentHypos;

    }

    public boolean checkMatchedSlots(Hypothesis hypothesis) {
        for(Slot slot: hypothesis.getSlots().values()) {
            if(slot.getCandidates().isEmpty()) {
                return false;
            }
        }
        return true;
    }



    abstract class AbsDialogFunction implements DialogFunction {
        private Object origin = DeviceControlDC.this;
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



}