package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.util.Vector3;

public class LocationReferenceResolver implements ReferenceResolver {

    private KnowledgeBase knowledgeBase;
    private String inputString = "";
    private KnowledgeMap personMap;

    public LocationReferenceResolver(KnowledgeBase knowledgeBase, String inputString) {
        this.knowledgeBase = knowledgeBase;
        this.inputString = inputString;
        this.personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
    }

    //TODO
    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution locationDistribution = new ReferenceDistribution();
        PersonReferenceResolver personReferenceResolver = new PersonReferenceResolver(knowledgeBase, inputString);

        if(inputString.contains("here") || inputString.contains("to me")) {
           ReferenceDistribution personDistribution = personReferenceResolver.getReferences();
           for(String id: personDistribution.getConfidences().keySet()) {
               if(personMap.get(id).get().get(Ontology.position).isPresent()) {
                   Vector3 position = personMap.get(id).get().get(Ontology.position).get();
                   //id for position??
                   locationDistribution.getConfidences().put(position.toString(), personDistribution.getConfidences().get(id));
                }

           }
       }
       return locationDistribution;
    }
}
