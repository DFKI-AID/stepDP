package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.core.WeightedReferenceResolver;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;

public class ObjectReferenceResolver implements ReferenceResolver {

    private KnowledgeMap personMap = new KnowledgeMap();
    private KnowledgeBase knowledgeBase;
    private String inputString = "";
    private String speakerId;

    public ObjectReferenceResolver(KnowledgeBase knowledgeBase, String inputString) {
        this.knowledgeBase = knowledgeBase;
        this.inputString = inputString;
        this.personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }
    //TODO
    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution objectDistribution = new ReferenceDistribution();

        knowledgeBase.getKnowledgeMap(Ontology.Entity);

        WeightedReferenceResolver weightedObjectResolvers = new WeightedReferenceResolver();
        //weightedObjectResolvers.addResolver();

        return objectDistribution;
    }
}
