package de.dfki.tocalog;

import de.dfki.tocalog.core.*;
import de.dfki.tocalog.model.Device;
import de.dfki.tocalog.model.Entity;
import de.dfki.tocalog.output.Imp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class LightControlComponent implements DialogComponent {
    private static Logger log = LoggerFactory.getLogger(LightControlComponent.class);
    private boolean lampOn = false;
    private final Imp imp;
    private TurnOnHypoProducer<Device> turnOnHypoProducer = new TurnOnHypoProducer();


    public LightControlComponent(Imp imp) {
        this.imp = imp;
    }

    @Override
    public Optional<DialogFunction> process(Inputs inputs) {
        Optional<Hypothesis> hOpt = parse(inputs);
        if(!hOpt.isPresent()) {
            return Optional.empty();
        }
        Hypothesis h = hOpt.get();

        if (!h.getIntent().equals(turnOnHypoProducer.getIntent())) {
            return Optional.empty();
        }

        Optional<Slot> slot = h.getSlot("target");
        if (!slot.isPresent()) {
            log.warn("could find slot 'target' in {} for {}", h, this.getClass().getSimpleName());
            return Optional.empty();
        }

        Collection<Entity> entities = slot.get().findMatches(imp.getKb());
        List<Device> devices = entities.stream()
                .filter(e -> e instanceof Device)
                .map(e -> (Device) e)
                .collect(Collectors.toList());

        //TODO filter if device is of or reduce confidence of DF

        if(devices.isEmpty()) {
            //TODO alternative: return with low confidence ("I could not find a device that ...")
            return Optional.empty();
        }

        return null;
    }

    protected void handleTurnOn() {

    }

    protected Optional<Hypothesis> parse(Inputs inputs) {
        return turnOnHypoProducer.process(inputs);
    }
}
