package de.dfki.step.resolution_entity;

import de.dfki.step.deprecated.kb.Entity;
import de.dfki.step.deprecated.kb.Ontology;
import de.dfki.step.kb.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class VisualFocusRR implements de.dfki.step.resolution_entity.ReferenceResolver {


    private long focusTimeout = 5000L;
    //person id for whom focus should be retrieved
    private String id;
    private Collection<Entity> foci;

    public VisualFocusRR(Supplier<Collection<Entity>> focusSupplier) {
        foci = focusSupplier.get();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public de.dfki.step.resolution_entity.ReferenceDistribution getReferences() {
        de.dfki.step.resolution_entity.ReferenceDistribution focusDistribution = new de.dfki.step.resolution_entity.ReferenceDistribution();

        List<Entity> focusList = foci.stream().filter(e -> e.get(Ontology.visualSource).orElse("").equals(id)).collect(Collectors.toList());
        long now = System.currentTimeMillis();

       /* Optional<Entity> latestFocus = focusList.stream().min( Comparator.comparing(e -> now - e.get(Ontology.timestamp).get()));
        if(latestFocus.isPresent()) {
            focusDistribution.getConfidences().put(latestFocus.get().get(Ontology.visualTarget).get(), latestFocus.get().get(Ontology.visualConfidence).get());
        }*/

       List<Entity> latestFoci = focusList.stream().filter(ent -> ent.get(Ontology.timestamp).orElse(0l) + focusTimeout >= now).collect(Collectors.toList());

        for(Entity e: latestFoci) {
            focusDistribution.getConfidences().put(e.get(Ontology.visualTarget).get(), e.get(Ontology.visualConfidence).get());
        }

        return focusDistribution.rescaleDistribution();
    }
}
