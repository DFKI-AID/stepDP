package de.dfki.tocalog.core.resolution;

import a.PersonDeixisResolver;
import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.core.WeightedReferenceResolver;
import de.dfki.tocalog.kb.*;

import java.util.HashMap;
import java.util.Map;

public class ObjectReferenceResolver implements ReferenceResolver {

    private KnowledgeMap personMap;
    private KnowledgeBase knowledgeBase;
    private String inputString = "";
    private String speakerId = "";
    private Type objectType;

    private Map<Attribute, AttributeValue> attrMap = new HashMap<>();

    public ObjectReferenceResolver(KnowledgeBase knowledgeBase, String inputString, Type objectType) {
        this.knowledgeBase = knowledgeBase;
        this.inputString = inputString;
        this.personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
        this.objectType = objectType;
    }

    public void setAttrMap(Map<Attribute, AttributeValue> attrMap) {
        this.attrMap = attrMap;
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public ReferenceDistribution getReferences() {

        if(inputString.contains("it")) {
            //TODO
            //check pointing
            //Check gaze
            //check discourse focus
        }

        ObjectAttributesReferenceResolver attrRR = new ObjectAttributesReferenceResolver(knowledgeBase, objectType);
        attrRR.setAttributes(attrMap);

        PossessiveObjectReferenceResolver possRR = new PossessiveObjectReferenceResolver(knowledgeBase, objectType);
        PersonReferenceResolver personRR = new PersonReferenceResolver(knowledgeBase, inputString);
        personRR.setSpeakerId(speakerId);
        possRR.setPersonDeixisResolver(personRR);

        ClosenessReferenceResolver closeRR = new ClosenessReferenceResolver(knowledgeBase, objectType);
        closeRR.setSpeakerId(speakerId);


        PointingReferenceResolver pointingRR = new PointingReferenceResolver(knowledgeBase, objectType);

        //TODO Gaze and Discourse

        WeightedReferenceResolver weightedObjectRR = new WeightedReferenceResolver();
        weightedObjectRR.addResolver(attrRR, 0.4);
        weightedObjectRR.addResolver(possRR, 0.3);
        weightedObjectRR.addResolver(closeRR, 0.1);
        weightedObjectRR.addResolver(pointingRR, 0.2);


        return weightedObjectRR.getReferences();
    }
}
