package de.dfki.step.core.resolution;

import de.dfki.step.core.Event;
import de.dfki.step.core.ReferenceDistribution;
import de.dfki.step.core.ReferenceResolver;
import de.dfki.step.core.SensorComponent;

/**
 * This resolver will increase the probability of devices that were used (touch screen, acc., ...)
 */
public class DeviceUseReferenceResolver implements ReferenceResolver, SensorComponent {
    @Override
    public ReferenceDistribution getReferences() {
        //TODO impl
        return null;
    }

    @Override
    public void process(Event event) {
        //TODO impl
    }
}