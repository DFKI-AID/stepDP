package de.dfki.step.core.resolution;

import de.dfki.step.core.ReferenceDistribution;
import de.dfki.step.core.ReferenceResolver;
import de.dfki.step.kb.*;

import java.util.Optional;


public class VisualFocusReferenceResolver implements ReferenceResolver {



    public VisualFocusReferenceResolver(KnowledgeBase kb) {
        this.visualFocusKL = kb.getKnowledgeList(Ontology.VisualFocus);
        this.kb = kb;
    }

    private long focusTimeout = 5000L;
    private KnowledgeList visualFocusKL;
    private String speakerId = "";
    private String type = "";
    private KnowledgeBase kb;

    public void setSpeakerId(String id) {
        this.speakerId = id;
    }


    public void setType(String type) {
        this.type = type;
    }

    @Override
    public ReferenceDistribution getReferences() {
        KnowledgeMap entityMap = kb.getKnowledgeMap(type);
        ReferenceDistribution focusDistribution = new ReferenceDistribution();

        visualFocusKL.removeOld(focusTimeout);

        for (Entity entity : entityMap.getAll()) {
            Optional<Entity> focusEntity = visualFocusKL.stream()
                    .filter(e -> e.get(Ontology.visualSource).orElse("").equals(speakerId))
                    .filter(e -> e.get(Ontology.visualTarget).orElse("").equals(entity.get(Ontology.id).get())).findFirst();
            if(focusEntity.isPresent()) {
                focusDistribution.getConfidences().put(entity.get(Ontology.id).get(), focusEntity.get().get(Ontology.visualConfidence).get());
            }else {
                focusDistribution.getConfidences().put(entity.get(Ontology.id).get(), 0.0);
            }

        }



        return focusDistribution.rescaleDistribution();
    }
}
