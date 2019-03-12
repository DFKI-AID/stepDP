package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;

public abstract class AbstractReferenceResolver implements ReferenceResolver {

    @Override
    public abstract ReferenceDistribution getReferences();

    public abstract void setSpeakerId(String speaker);

    public abstract void setEntityType(String type);

    public abstract void setInputString(String inputString);
}
