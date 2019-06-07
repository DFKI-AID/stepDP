package de.dfki.step.resolution;

import de.dfki.step.kb.DataEntry;
import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/*candidate entities get higher confidence if they have been mentioned recently in the discourse */
public class DiscourseFocusRR implements ReferenceResolver {


    private long discourseTimeout = 5000L;
    private Collection<DataEntry> discourseFoci;
    private Collection<DataEntry> candidateEntities;


    public DiscourseFocusRR(Supplier<Collection<DataEntry>> discourseSupplier, Supplier<Collection<DataEntry>> candidateEntitiesSupplier) {
        this.discourseFoci = discourseSupplier.get();
        this.candidateEntities = candidateEntitiesSupplier.get();
    }



    @Override
    public ReferenceDistribution getReferences() {

        ReferenceDistribution discourseDistribution = new ReferenceDistribution();
        long now = System.currentTimeMillis();

        for (DataEntry entity : candidateEntities) {
            Optional<DataEntry> discourseEntity = discourseFoci.stream()
                    .filter(e -> e.get("discourseTarget").get().equals(entity.getId())).findFirst();
            if(discourseEntity.isPresent() && discourseEntity.get().get("timestamp", Long.class).orElse(0L) + discourseTimeout >= now) {
                discourseDistribution.getConfidences().put(entity.getId(), discourseEntity.get().get("confidence", Double.class).get());
            }else {
                discourseDistribution.getConfidences().put(entity.getId(), 0.0);
            }

        }


        return discourseDistribution.rescaleDistribution();
    }

}
