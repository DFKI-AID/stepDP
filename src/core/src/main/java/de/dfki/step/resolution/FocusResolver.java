package de.dfki.step.resolution;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.Ontology;
import de.dfki.step.rengine.Rule;
import de.dfki.step.resolution.ReferenceDistribution;
import de.dfki.step.resolution.ReferenceResolver;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Multiple components (e.g. kinect or hololens) write the focus of an agent from their point of view into the KB.
 * This handler will merge the foci into one reference distribution for an agent.
 */
public class FocusResolver implements ReferenceResolver {

    public FocusResolver(Supplier<Collection<Entity>> personSupplier) {

    }

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public ReferenceDistribution getReferences() {
        throw new UnsupportedOperationException("not impl");
    }

    public static void main(String[] args) {
        KnowledgeBase kb = null;
        var fr = new FocusResolver(() -> {
            return kb.getKnowledgeMap(Ontology.Person).getAll();
        });

        Rule rule = () -> {
            fr.setId("m1");
            ReferenceDistribution foci = fr.getReferences();

        };
    }
}
