package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.kb.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DiscourseReferenceResolver implements ReferenceResolver{




    public DiscourseReferenceResolver(KnowledgeBase kb) {
        this.discourseFocusKL = kb.getKnowledgeList(Ontology.DiscourseFocus);
        this.kb = kb;
    }

    private long discourseTimeout = 5000L;
    private KnowledgeList discourseFocusKL;
    private String type = "";
    private KnowledgeBase kb;


    public void setType(String type) {
        this.type = type;
    }

    @Override
    public ReferenceDistribution getReferences() {
        KnowledgeMap entityMap = kb.getKnowledgeMap(type);

        //TODO: instead of timeout, confidence should gradually be reduced when time passes without re-mentioning
        discourseFocusKL.removeOld(discourseTimeout);

        ReferenceDistribution discourseDistribution = new ReferenceDistribution();
        for (Entity entity : entityMap.getAll()) {
            Optional<Entity> discourseEntity = discourseFocusKL.getAll().stream().filter(e -> e.get(Ontology.discourseTarget).get().equals(entity.get(Ontology.id).get())).findFirst();
            if(discourseEntity.isPresent()) {
                discourseDistribution.getConfidences().put(entity.get(Ontology.id).get(), discourseEntity.get().get(Ontology.discourseConfidence).get());
            }else {
                discourseDistribution.getConfidences().put(entity.get(Ontology.id).get(), 0.0);
            }

        }


        return discourseDistribution.rescaleDistribution();
    }

}
