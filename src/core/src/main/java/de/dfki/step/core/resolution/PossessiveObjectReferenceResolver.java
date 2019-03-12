package de.dfki.step.core.resolution;

import de.dfki.step.core.ReferenceDistribution;
import de.dfki.step.core.ReferenceResolver;
import de.dfki.step.core.WeightedReferenceResolver;
import de.dfki.step.kb.*;

import java.util.Collection;
import java.util.stream.Collectors;

public class PossessiveObjectReferenceResolver implements ReferenceResolver {


    private ReferenceResolver personDeixisResolver = new WeightedReferenceResolver();
    private KnowledgeMap personMap;
    private KnowledgeMap objectMap = new KnowledgeMap();
    private KnowledgeBase kb;
    private String objectType = "";


    public PossessiveObjectReferenceResolver(KnowledgeBase knowledgeBase) {
        this.kb = knowledgeBase;
        personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);

    }

    public void setPersonDeixisResolver(ReferenceResolver personDeixisResolver) {
        this.personDeixisResolver = personDeixisResolver;
    }


    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    @Override
    public ReferenceDistribution getReferences() {
        objectMap = kb.getKnowledgeMap(objectType);
        ReferenceDistribution objectDistribution = new ReferenceDistribution();


        ReferenceDistribution personDistribution = personDeixisResolver.getReferences();

       //double restConfidenceCount = 0.0;

        for(String personId: personDistribution.getConfidences().keySet()) {
            Collection<Entity> ownedObjects = objectMap.getAll().stream().filter(o -> o.get(Ontology.owner).orElse(new Reference("", "")).id.equals(personId)).collect(Collectors.toList());
            for(Entity obj: ownedObjects) {
                objectDistribution.getConfidences().put(obj.get(Ontology.id).get(), personDistribution.getConfidences().get(personId));
            }
        }


       /* for(Entity obj: objectMap.getAll()) {
            if(!objectDistribution.getConfidences().keySet().contains(obj.get(Ontology.id).get())) {
                objectDistribution.getConfidences().put(obj.get(Ontology.id).get(), 1.0/objectMap.getAll().size());
            }
        }*/

       /* for(String personId: personDistribution.getConfidences().keySet()) {
            if(personMap.get(personId).get().get(Ontology.owned).isPresent()) {
                //TODO use owner or owned?
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
        for(String id: objectDistribution.getConfidences().keySet()) {
            double newValue = objectDistribution.getConfidences().get(id) + restConfidenceCount/objectDistribution.getConfidences().size();
            objectDistribution.getConfidences().put(id,newValue);
        }*/

        //no person with device
        if(objectDistribution.getConfidences().isEmpty()) {
            for(Entity obj: objectMap.getAll()) {
                objectDistribution.getConfidences().put(obj.get(Ontology.id).get(), 1.0/objectMap.getAll().size());
            }
        }
        objectDistribution.rescaleDistribution();

        return objectDistribution;

    }
}
