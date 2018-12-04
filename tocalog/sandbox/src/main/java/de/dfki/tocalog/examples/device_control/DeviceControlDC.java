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
        slot2.setSlotConstraint(new Slot.SlotRangeConstraint(List.of("blue", "green", "red", "yelow")));
        changeColorBuilder.addSlot(slot);
        deviceControlHypos.add(changeColorBuilder.build());

        //Increase_brightness _DEV _Brightness
        Hypothesis.Builder increaseBrightnessBuilder = new Hypothesis.Builder("increaseBrightness");
        slot = new Slot("dev");
        slot.setSlotConstraint(new Slot.SlotTypeConstraint(Ontology.Device));
        slot2 = new Slot("brightness");
        slot.setSlotConstraint(new Slot.NumericSlotConstraint(0.0, 360.0));
        increaseBrightnessBuilder.addSlot(slot);
        deviceControlHypos.add(increaseBrightnessBuilder.build());

        //Decrease_brightness _DEV _Brightness
        Hypothesis.Builder decreaseBrightnessBuilder = new Hypothesis.Builder("decreaseBrightness");
        slot = new Slot("dev");
        slot.setSlotConstraint(new Slot.SlotTypeConstraint(Ontology.Device));
        slot2 = new Slot("brightness");
        slot.setSlotConstraint(new Slot.NumericSlotConstraint(0.0, 360.0));
        decreaseBrightnessBuilder.addSlot(slot);
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

        return Optional.empty();
    }
}
