package de.dfki.tocalog;

import de.dfki.tocalog.core.*;
import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.model.Device;
import de.dfki.tocalog.model.Entity;
import de.dfki.tocalog.output.IMPP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simulates simple device control
 */
public class DeviceControlBehavior implements DialogComponent {
    private static Logger log = LoggerFactory.getLogger(DeviceControlBehavior.class);
    private static final String INTENT = "TurnOn";
    private boolean fanOn = false;
    private boolean tvOn = false;
    private boolean radioOn = false;
    private boolean lampOn = false;
    private final IMPP imp;

    public DeviceControlBehavior(IMPP imp) {
        this.imp = imp;
    }

    @Override
    public Optional<DialogFunction> process(Hypothesis h) {
        h.getIntent();

        if (!h.getIntent().equals(INTENT)) {
            return Optional.empty();
        }

        Optional<Hypothesis.Slot> slot = h.getSlot("target");
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

        return Optional.of(() -> {
            for (Device device : devices) {
                log.info("turning on \"{}\"", device);
            }
        });
    }

    @Override
    public Collection<Class<HypothesisProducer>> getRelevantHypoProducers() {
        return Collections.EMPTY_SET; //TODO
    }
}
