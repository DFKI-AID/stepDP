package de.dfki.step.resolution_entity;

import de.dfki.step.kb.*;

import java.util.*;
import java.util.function.Supplier;

/* resolves references to object -> weighted distribution is used, for example given attributes and pronouns are considered */
public class ObjectRR implements ReferenceResolver {


    private String speakerId = "";
    private Collection<Entity> candidateObjects;
    private Map<Attribute, AttributeValue> attrMap = new HashMap<>();
    private String pronoun;
    private Supplier<Collection<Entity>> personSupplier;
    private Supplier<Collection<Entity>> objectSupplier;
    private Supplier<Collection<Entity>> sessionSupplier;

    public ObjectRR(Supplier<Collection<Entity>> objectSupplier) {
        this.objectSupplier = objectSupplier;
        this.candidateObjects = objectSupplier.get();
    }

    public void setAttrMap(Map<Attribute, AttributeValue> attrMap) {
        this.attrMap = attrMap;
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }


    public void setPronoun(String pronoun) {
        this.pronoun = pronoun;
    }

    public void setPersonSupplier(Supplier<Collection<Entity>> personSupplier) {
        this.personSupplier = personSupplier;
    }

    public void setSessionSupplier(Supplier<Collection<Entity>> sessionSupplier) {
        this.sessionSupplier = sessionSupplier;
    }


    @Override
    public ReferenceDistribution getReferences() {
        Map<ReferenceResolver, Double> referenceWeightMap = new HashMap<>();

        if(!attrMap.isEmpty()) {
            AttributeRR attrRR = new AttributeRR(objectSupplier);
            attrRR.setAttributes(attrMap);
            referenceWeightMap.put(attrRR, 3.0);
        }

        if(personSupplier != null && sessionSupplier != null && pronoun != null) {
            PossessiveRR possRR = new PossessiveRR(objectSupplier);

            PersonPronounRR personRR = new PersonPronounRR(personSupplier, sessionSupplier);
            personRR.setSpeakerId(speakerId);
            personRR.setPronoun(pronoun);
            possRR.setPossessivePronounResolver(personRR);

            referenceWeightMap.put(possRR, 2.0);

        }


         //VisualFocus, DiscourseFocus, Closeness, ... could be added here as well


       if(referenceWeightMap.isEmpty()) {
           ReferenceDistribution distribution = new ReferenceDistribution();
           for(Entity e: candidateObjects) {
               distribution.getConfidences().put(e.get(Ontology.id).get(), 1.0/candidateObjects.size());
           }
           return distribution;

       }

        WeightedRR weightedObjectRR = new WeightedRR();
        Optional<Double> count = referenceWeightMap.values().stream().reduce((d1, d2) -> d1 + d2);
        if(count.isPresent()) {
            for(ReferenceResolver resolver: referenceWeightMap.keySet()) {
                weightedObjectRR.addResolver(resolver, referenceWeightMap.get(resolver)/count.get());
            }
        }



        return weightedObjectRR.getReferences();
    }


}
