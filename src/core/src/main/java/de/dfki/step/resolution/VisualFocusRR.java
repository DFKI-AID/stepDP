package de.dfki.step.resolution;

import de.dfki.step.kb.DataEntry;
import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class VisualFocusRR implements ReferenceResolver {


    private long focusTimeout = 5000L;
    //person id for whom focus should be retrieved
    private String id;
    private Collection<DataEntry> foci;

    public VisualFocusRR(Supplier<Collection<DataEntry>> focusSupplier) {
        foci = focusSupplier.get();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution focusDistribution = new ReferenceDistribution();

        List<DataEntry> focusList = foci.stream().filter(e -> e.get("visualSource").orElse("").equals(id)).collect(Collectors.toList());
        long now = System.currentTimeMillis();

       /* Optional<Entity> latestFocus = focusList.stream().min( Comparator.comparing(e -> now - e.get(Ontology.timestamp).get()));
        if(latestFocus.isPresent()) {
            focusDistribution.getConfidences().put(latestFocus.get().get(Ontology.visualTarget).get(), latestFocus.get().get(Ontology.visualConfidence).get());
        }*/

       List<DataEntry> latestFoci = focusList.stream().filter(ent -> ent.get("timestamp", Long.class).orElse(0l) + focusTimeout >= now).collect(Collectors.toList());

        for(DataEntry e: latestFoci) {
            focusDistribution.getConfidences().put(e.get("visualTarget", String.class).get(), e.get("visualConfidence", Double.class).get());
        }

        return focusDistribution.rescaleDistribution();
    }
}
