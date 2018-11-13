package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.core.WeightedReferenceResolver;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PersonReferenceResolver implements ReferenceResolver {

    private KnowledgeBase knowledgeBase;
    private String inputString = "";
    private String speakerId = "";
    private KnowledgeMap personMap = new KnowledgeMap();

    public PersonReferenceResolver(KnowledgeBase knowledgeBase, String inputString) {
        this.knowledgeBase = knowledgeBase;
        this.inputString = inputString;
        this.personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }


    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();
        Map<String, WeightedReferenceResolver> pronoun2ReferenceResolverMap = getPronoun2ReferenceResolver();

        if(inputString.contains("I") || inputString.contains("me") || inputString.contains("my") || inputString.contains("mine")) {
            distribution = pronoun2ReferenceResolverMap.get("I").getReferences();
        }else if(inputString.contains("you") || inputString.contains("your")|| inputString.contains("yours")) {
            distribution = pronoun2ReferenceResolverMap.get("you").getReferences();
        }else if((inputString.contains("he") && !inputString.contains("they")) || inputString.contains("his") || inputString.contains("him")) {
            distribution = pronoun2ReferenceResolverMap.get("he").getReferences();
        }else if(inputString.contains("she") || inputString.contains("her") || inputString.contains("hers")) {
            distribution = pronoun2ReferenceResolverMap.get("she").getReferences();
        }else if(inputString.contains("we") || inputString.contains("us") || inputString.contains("our") || inputString.contains("ours")) {
            distribution = pronoun2ReferenceResolverMap.get("we").getReferences();
        }else if(inputString.contains("they") || inputString.contains("their") || inputString.equals("theirs") || inputString.contains("them")) {
            distribution = pronoun2ReferenceResolverMap.get("they").getReferences();
        }else {
            Collection<Entity> persons = personMap.query(e -> inputString.contains(e.get(Ontology.name).orElse("")));
            for(Entity person: persons) {
                distribution.getConfidences().put(person.get(Ontology.id).get(), 1.0/persons.size());
            }
        }

        return distribution;
    }


    public Map<String, WeightedReferenceResolver> getPronoun2ReferenceResolver() {

        Map<String, WeightedReferenceResolver> pronounToRefResolverMap = new HashMap<>();
        SpeakerReferenceResolver speakerReferenceResolver = new SpeakerReferenceResolver(knowledgeBase);
        speakerReferenceResolver.setSpeakerId(speakerId);

        SessionReferenceResolver sessionReferenceResolver = new SessionReferenceResolver(knowledgeBase);
        sessionReferenceResolver.setSpeakerId(speakerId);

        NotSpeakerSessionReferenceResolver notSpeakersessionReferenceResolver = new NotSpeakerSessionReferenceResolver(knowledgeBase);
        notSpeakersessionReferenceResolver.setSpeakerId(speakerId);

        //TODO different foci

        WeightedReferenceResolver weightedReferenceResolver_I = new WeightedReferenceResolver();
        weightedReferenceResolver_I.addResolver(speakerReferenceResolver, 1.0);

        //should weight sum up to 1?
        WeightedReferenceResolver weightedReferenceResolver_you  = new WeightedReferenceResolver();
        weightedReferenceResolver_you.addResolver(new ReverseReferenceResolver(speakerReferenceResolver), 0.5);
        weightedReferenceResolver_you.addResolver(sessionReferenceResolver, 0.5);
       // weightedReferenceResolver_you.addResolver(new GazeReferenceResolver(knowledgeBase), 0.1);

        WeightedReferenceResolver weightedReferenceResolver_he  = new WeightedReferenceResolver();
        weightedReferenceResolver_he.addResolver(new ReverseReferenceResolver(speakerReferenceResolver), 0.2);
        GenderReferenceResolver maleReferenceResolver = new GenderReferenceResolver(knowledgeBase);
        maleReferenceResolver.setGender("male");
        weightedReferenceResolver_he.addResolver(maleReferenceResolver, 0.7);
        weightedReferenceResolver_he.addResolver(sessionReferenceResolver, 0.1);
      //  weightedReferenceResolver_he.addResolver(new GazeReferenceResolver(knowledgeBase), 0.3);

        WeightedReferenceResolver weightedReferenceResolver_she  = new WeightedReferenceResolver();
        weightedReferenceResolver_she.addResolver(new ReverseReferenceResolver(speakerReferenceResolver), 0.2);
        GenderReferenceResolver femaleReferenceResolver = new GenderReferenceResolver(knowledgeBase);
        femaleReferenceResolver.setGender("female");
        weightedReferenceResolver_she.addResolver(femaleReferenceResolver, 0.7);
        weightedReferenceResolver_she.addResolver(sessionReferenceResolver, 0.1);
     //   weightedReferenceResolver_she.addResolver(new GazeReferenceResolver(knowledgeBase), 0.3);

        WeightedReferenceResolver weightedReferenceResolver_we  = new WeightedReferenceResolver();
        weightedReferenceResolver_we.addResolver(sessionReferenceResolver, 1.0);

        WeightedReferenceResolver weightedReferenceResolver_they  = new WeightedReferenceResolver();

        weightedReferenceResolver_they.addResolver(new ReverseReferenceResolver(speakerReferenceResolver), 0.5);
        weightedReferenceResolver_they.addResolver(notSpeakersessionReferenceResolver, 0.5);
     //   weightedReferenceResolver_they.addResolver(new GazeReferenceResolver(knowledgeBase), 0.3);


        pronounToRefResolverMap.put("I", weightedReferenceResolver_I);
        pronounToRefResolverMap.put("you", weightedReferenceResolver_you);
        pronounToRefResolverMap.put("he", weightedReferenceResolver_he);
        pronounToRefResolverMap.put("she", weightedReferenceResolver_she);
        pronounToRefResolverMap.put("we", weightedReferenceResolver_we);
        pronounToRefResolverMap.put("they", weightedReferenceResolver_they);

        return pronounToRefResolverMap;
    }
}
