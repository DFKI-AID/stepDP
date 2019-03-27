package de.dfki.step.resolution;

import de.dfki.step.kb.*;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/*candidate entities get higher confidence if they have been mentioned recently in the discourse */
public class DiscourseFocusRR implements ReferenceResolver{


    private long discourseTimeout = 5000L;
    private Collection<Entity> discourseFoci;
    private Collection<Entity> candidateEntities;


    public DiscourseFocusRR(Supplier<Collection<Entity>> discourseSupplier, Supplier<Collection<Entity>> candidateEntitiesSupplier) {
        this.discourseFoci = discourseSupplier.get();
        this.candidateEntities = candidateEntitiesSupplier.get();
    }



    @Override
    public ReferenceDistribution getReferences() {

        ReferenceDistribution discourseDistribution = new ReferenceDistribution();
        long now = System.currentTimeMillis();

        for (Entity entity : candidateEntities) {
            Optional<Entity> discourseEntity = discourseFoci.stream()
                    .filter(e -> e.get(Ontology.discourseTarget).get().equals(entity.get(Ontology.id).get())).findFirst();
            if(discourseEntity.isPresent() && discourseEntity.get().get(Ontology.timestamp).orElse(0L) + discourseTimeout >= now) {
                discourseDistribution.getConfidences().put(entity.get(Ontology.id).get(), discourseEntity.get().get(Ontology.discourseConfidence).get());
            }else {
                discourseDistribution.getConfidences().put(entity.get(Ontology.id).get(), 0.0);
            }

        }


        return discourseDistribution.rescaleDistribution();
    }

}
