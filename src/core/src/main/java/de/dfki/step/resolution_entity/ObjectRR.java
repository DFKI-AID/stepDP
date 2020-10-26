package de.dfki.step.resolution_entity;

import de.dfki.step.kb.*;

import java.util.*;
import java.util.function.Supplier;

/* resolves references to object -> weighted distribution is used, for example given attributes and pronouns are considered */
public class ObjectRR implements de.dfki.step.resolution_entity.ReferenceResolver {


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
    public de.dfki.step.resolution_entity.ReferenceDistribution getReferences() {
        Map<de.dfki.step.resolution_entity.ReferenceResolver, Double> referenceWeightMap = new HashMap<>();

        if(!attrMap.isEmpty()) {
            de.dfki.step.resolution_entity.AttributeRR attrRR = new de.dfki.step.resolution_entity.AttributeRR(objectSupplier);
            attrRR.setAttributes(attrMap);
            referenceWeightMap.put(attrRR, 3.0);
        }

        if(personSupplier != null && sessionSupplier != null && pronoun != null) {
            de.dfki.step.resolution_entity.PossessiveRR possRR = new de.dfki.step.resolution_entity.PossessiveRR(objectSupplier);

            de.dfki.step.resolution_entity.PersonPronounRR personRR = new de.dfki.step.resolution_entity.PersonPronounRR(personSupplier, sessionSupplier);
            personRR.setSpeakerId(speakerId);
            personRR.setPronoun(pronoun);
            possRR.setPossessivePronounResolver(personRR);

            referenceWeightMap.put(possRR, 2.0);

        }


         //VisualFocus, DiscourseFocus, Closeness, ... could be added here as well


       if(referenceWeightMap.isEmpty()) {
           de.dfki.step.resolution_entity.ReferenceDistribution distribution = new de.dfki.step.resolution_entity.ReferenceDistribution();
           for(Entity e: candidateObjects) {
               distribution.getConfidences().put(e.get(Ontology.id).get(), 1.0/candidateObjects.size());
           }
           return distribution;

       }

        de.dfki.step.resolution_entity.WeightedRR weightedObjectRR = new de.dfki.step.resolution_entity.WeightedRR();
        Optional<Double> count = referenceWeightMap.values().stream().reduce((d1, d2) -> d1 + d2);
        if(count.isPresent()) {
            for(de.dfki.step.resolution_entity.ReferenceResolver resolver: referenceWeightMap.keySet()) {
                weightedObjectRR.addResolver(resolver, referenceWeightMap.get(resolver)/count.get());
            }
        }



        return weightedObjectRR.getReferences();
    }


}
