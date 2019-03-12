package de.dfki.tocalog.core;

import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeList;
import de.dfki.tocalog.kb.Ontology;

//TODO: wirklich eigene Klasse? Wo hin damit?
public class ReferenceMarker {

    private KnowledgeList discourseFocusKL;


    public ReferenceMarker(KnowledgeBase kb) {
        this.discourseFocusKL = kb.getKnowledgeList(Ontology.DiscourseFocus);
    }

    public void markHypothesisEntites(Hypothesis hypothesis) {
        for(Slot slot: hypothesis.getSlots().values()) {
            if(slot.getFinalSlotEntity().isPresent()) {
                Entity slotEntity = slot.getFinalSlotEntity().get();
                Entity discourseEntity = new Entity();
                discourseEntity.set(Ontology.discourseTarget, slotEntity.get(Ontology.id).get());
                discourseEntity.set(Ontology.discourseConfidence, 1.0);
                this.discourseFocusKL.add(discourseEntity);
            }
        }
    }

    //TODO: update discourse focus for entities mentioned in output
}


