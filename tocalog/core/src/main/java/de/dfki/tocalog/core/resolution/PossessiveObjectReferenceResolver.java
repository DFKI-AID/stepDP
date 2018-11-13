package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.core.WeightedReferenceResolver;
import de.dfki.tocalog.kb.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.dfki.tocalog.kb.Ontology.gender;

public class PossessiveObjectReferenceResolver implements ReferenceResolver {


    private WeightedReferenceResolver personDeixisResolver = new WeightedReferenceResolver();
    private KnowledgeMap personMap;
    private KnowledgeMap objectMap;


    public PossessiveObjectReferenceResolver(KnowledgeBase knowledgeBase, Type objectType) {
        personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
        objectMap = knowledgeBase.getKnowledgeMap(objectType);
    }

    public void setPersonDeixisResolver(WeightedReferenceResolver personDeixisResolver) {
        this.personDeixisResolver = personDeixisResolver;
    }




    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution objectDistribution = new ReferenceDistribution();


        ReferenceDistribution personDistribution = personDeixisResolver.getReferences();

        double restConfidenceCount = 0.0;

        for(String personId: personDistribution.getConfidences().keySet()) {
            if(personMap.get(personId).get().get(Ontology.owned).isPresent()) {
                String ownedObjectId = personMap.get(personId).get().get(Ontology.owned).get();
                //check if ownedObject has Type of referenced object
                if (objectMap.get(ownedObjectId).isPresent()) {
                    //give object the confidence of the referenced possesive pronoun
                    objectDistribution.getConfidences().put(ownedObjectId, personDistribution.getConfidences().get(personId));
                }else{
                    restConfidenceCount += personDistribution.getConfidences().get(personId);
                }
            }else {
                restConfidenceCount += personDistribution.getConfidences().get(personId);
            }
        }
        //distribute remaining confidence from persons without objects equally on candidates
        for(String id: personDistribution.getConfidences().keySet()) {
            double newValue = objectDistribution.getConfidences().get(id) + restConfidenceCount/objectDistribution.getConfidences().size();
            objectDistribution.getConfidences().put(id,newValue);
        }


        return objectDistribution;

    }
}
