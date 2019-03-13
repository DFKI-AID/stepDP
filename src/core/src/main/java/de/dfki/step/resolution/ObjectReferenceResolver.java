package de.dfki.step.resolution;

import de.dfki.step.core.ReferenceDistribution;
import de.dfki.step.core.ReferenceResolver;
import de.dfki.step.core.WeightedReferenceResolver;
import de.dfki.step.kb.*;

import java.util.*;

public class ObjectReferenceResolver extends AbstractReferenceResolver {

    private KnowledgeBase knowledgeBase;
    private String inputString = "";
    private String speakerId = "";
    private String objectType;
    private Map<ReferenceResolver, Double> referenceWeightMap = new HashMap<>();
    private static Map<String, String> ATTRIBUTES;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("white", "color");
        map.put("red", "color");
        map.put("green", "color");
        map.put("yellow", "color");
        map.put("blue", "color");
        map.put("big", "size");
        map.put("small", "size");
        map.put("medium", "size");

        ATTRIBUTES = Collections.unmodifiableMap(map);
    }


    private Map<Attribute, AttributeValue> attrMap = new HashMap<>();

    public ObjectReferenceResolver(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public void setAttrMap(Map<Attribute, AttributeValue> attrMap) {
        this.attrMap = attrMap;
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public void setEntityType(String type) {
        this.objectType = type;
    }

    @Override
    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    @Override
    public ReferenceDistribution getReferences() {

        for(String attr: ATTRIBUTES.keySet()) {
            if (inputString.contains(attr)) {
                if(ATTRIBUTES.get(attr).equals("color")) {
                    AttributeValue attrVal = new AttributeValue<>(Ontology.color.name, attr, Ontology.color);
                    attrMap.put(Ontology.color, attrVal);
                }else if(ATTRIBUTES.get(attr).equals("size")) {
                    AttributeValue attrVal = new AttributeValue<>(Ontology.size.name, attr, Ontology.size);
                    attrMap.put(Ontology.size, attrVal);
                }
            }
        }


        if(inputString.contains("it")) {
            //TODO
            //check pointing
            //Check gaze
            //check discourse focus
        }

        if(!attrMap.isEmpty()) {
            ObjectAttributesReferenceResolver attrRR = new ObjectAttributesReferenceResolver(knowledgeBase);
            attrRR.setType(objectType);
            attrRR.setAttributes(attrMap);
            referenceWeightMap.put(attrRR, 3.0);
        }

        for(String pronoun: PersonReferenceResolver.PRONOUNS.keySet()) {
            if(inputString.contains(" " + pronoun + " ")) {
                PossessiveObjectReferenceResolver possRR = new PossessiveObjectReferenceResolver(knowledgeBase);
                possRR.setObjectType(objectType);

                PersonReferenceResolver personRR = new PersonReferenceResolver(knowledgeBase);
                personRR.setInputString(inputString);
                personRR.setSpeakerId(speakerId);
                possRR.setPersonDeixisResolver(personRR);

                referenceWeightMap.put(possRR, 2.0);
            }
        }



    /*    ClosenessReferenceResolver closeRR = new ClosenessReferenceResolver(knowledgeBase);
        closeRR.setEntityType(objectType);
        closeRR.setSpeakerId(speakerId);


        PointingReferenceResolver pointingRR = new PointingReferenceResolver(knowledgeBase);
        pointingRR.setObjectType(objectType);
        */

        //TODO Gaze and Discourse


       /* WeightedReferenceResolver weightedObjectRR = new WeightedReferenceResolver();
        weightedObjectRR.addResolver(attrRR, 0.4); //3x
        weightedObjectRR.addResolver(possRR, 0.3); //2x
        weightedObjectRR.addResolver(closeRR, 0.1); //1x
        weightedObjectRR.addResolver(pointingRR, 0.2); //1x*/


       if(referenceWeightMap.isEmpty()) {
           ReferenceDistribution distribution = new ReferenceDistribution();
           KnowledgeMap objectMap = knowledgeBase.getKnowledgeMap(objectType);
           for(Entity e: objectMap.getAll()) {
               distribution.getConfidences().put(e.get(Ontology.id).get(), 1.0/objectMap.getAll().size());
           }
           return distribution;

       }

        WeightedReferenceResolver weightedObjectRR = new WeightedReferenceResolver();
        Optional<Double> count = referenceWeightMap.values().stream().reduce((d1, d2) -> d1 + d2);
        if(count.isPresent()) {
            for(ReferenceResolver resolver: referenceWeightMap.keySet()) {
                weightedObjectRR.addResolver(resolver, referenceWeightMap.get(resolver)/count.get());
            }
        }



        return weightedObjectRR.getReferences();
    }
}
