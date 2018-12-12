package de.dfki.tocalog.examples.device_control;

import de.dfki.tocalog.core.HypothesisProcessor;
import de.dfki.tocalog.core.*;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.kb.Ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeviceControlDC implements DialogComponent {
    List<Hypothesis> deviceControlHypos = new ArrayList<>();
    List<HypothesisProcessor> processors = new ArrayList<>();

    public DeviceControlDC( List<HypothesisProcessor> processors) {
        this.processors = processors;
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
                Hypothesis processedHypo = processor.process(currentInput, hypo);
                System.out.println("processedHypo" + processedHypo.toString());
            }
        }

        //check if for current input all processors match same hypo intent

        //check slot candidates: same resolved entities for several processors, i.e. if different and confidence for both high -> clarification question

        //if not all slots filled: give prefilled hypo to processor so that they can add slot values...

        return Optional.empty();
    }
}
