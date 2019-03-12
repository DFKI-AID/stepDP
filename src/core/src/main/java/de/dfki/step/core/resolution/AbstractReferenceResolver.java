package de.dfki.step.core.resolution;

import de.dfki.step.core.ReferenceDistribution;
import de.dfki.step.core.ReferenceResolver;

public abstract class AbstractReferenceResolver implements ReferenceResolver {

    @Override
    public abstract ReferenceDistribution getReferences();

    public abstract void setSpeakerId(String speaker);

    public abstract void setEntityType(String type);

    public abstract void setInputString(String inputString);
}
