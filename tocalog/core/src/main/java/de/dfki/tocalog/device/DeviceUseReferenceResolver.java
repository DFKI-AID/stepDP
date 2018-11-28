package de.dfki.tocalog.device;

import de.dfki.tocalog.core.Event;
import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.core.SensorComponent;

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
