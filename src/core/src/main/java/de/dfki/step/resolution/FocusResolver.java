package de.dfki.step.resolution;

import de.dfki.step.resolution.ReferenceDistribution;
import de.dfki.step.resolution.ReferenceResolver;

/**
 * Multiple components (e.g. kinect or hololens) write the focus of an agent from their point of view into the KB.
 * This handler will merge the foci into one reference distribution for an agent.
 */
public class FocusResolver implements ReferenceResolver {

    @Override
    public ReferenceDistribution getReferences() {
        throw new UnsupportedOperationException("not impl");
    }
}
