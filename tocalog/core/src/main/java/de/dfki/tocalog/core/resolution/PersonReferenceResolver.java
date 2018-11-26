package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.core.WeightedReferenceResolver;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PersonReferenceResolver implements ReferenceResolver {

    private KnowledgeBase knowledgeBase;
    private String inputString = "";
    private String speakerId = "";
    private KnowledgeMap personMap = new KnowledgeMap();
    private SpeakerReferenceResolver speakerRR;
    private SessionReferenceResolver sessionRR;

    private static final Map<String, String> PRONOUNS;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("I", "I");
        map.put("me", "I");
        map.put("my", "I");
        map.put("mine", "I");
        map.put("you", "you");
        map.put("your", "you");
        map.put("yours", "you");
        map.put("he", "he");
        map.put("his", "he");
        map.put("him", "he");
        map.put("she", "she");
        map.put("her", "she");
        map.put("hers", "she");
        map.put("we", "we");
        map.put("us", "we");
        map.put("our", "we");
        map.put("ours", "we");
        map.put("they", "they");
        map.put("their", "they");
        map.put("theirs", "they");
        map.put("them", "they");

        PRONOUNS = Collections.unmodifiableMap(map);
    }

    public PersonReferenceResolver(KnowledgeBase knowledgeBase, String inputString) {
        this.knowledgeBase = knowledgeBase;
        this.inputString = inputString;
        this.personMap = knowledgeBase.getKnowledgeMap(Ontology.Person);
        speakerRR = new SpeakerReferenceResolver(knowledgeBase);
        sessionRR = new SessionReferenceResolver(knowledgeBase);


    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }


    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();
        Map<String, WeightedReferenceResolver> pronoun2ReferenceResolverMap = getPronoun2ReferenceResolver();

        //check pronouns
        for(String pronoun: PRONOUNS.keySet()) {
            if(inputString.contains(pronoun)) {
                return pronoun2ReferenceResolverMap.get(PRONOUNS.get(pronoun)).getReferences();
            }
        }

        //check if person's name is given
        Collection<Entity> persons = personMap.query(e -> inputString.equalsIgnoreCase(e.get(Ontology.name).orElse("")));
        for(Entity person: persons) {
            distribution.getConfidences().put(person.get(Ontology.id).get(), 1.0/persons.size());
        }


        return distribution;
    }

    private WeightedReferenceResolver getIResolver() {
        WeightedReferenceResolver weightedRR_I = new WeightedReferenceResolver();
        weightedRR_I.addResolver(speakerRR, 1.0);
        return weightedRR_I;
    }

    private WeightedReferenceResolver getYouResolver() {
        WeightedReferenceResolver weightedRR_you  = new WeightedReferenceResolver();
        weightedRR_you.addResolver(new ReverseReferenceResolver(speakerRR), 0.5);
        weightedRR_you.addResolver(sessionRR, 0.5);
        return weightedRR_you;
    }

    private WeightedReferenceResolver getGenderResolver(String gender) {
        WeightedReferenceResolver weightedRR_gender  = new WeightedReferenceResolver();
        weightedRR_gender.addResolver(new ReverseReferenceResolver(speakerRR), 0.2);
        GenderReferenceResolver genderRR = new GenderReferenceResolver(knowledgeBase);
        genderRR.setGender(gender);
        weightedRR_gender.addResolver(genderRR, 0.7);
        weightedRR_gender.addResolver(sessionRR, 0.1);
        return weightedRR_gender;
    }


    private WeightedReferenceResolver getWeResolver() {
        WeightedReferenceResolver weightedRR_we  = new WeightedReferenceResolver();
        weightedRR_we.addResolver(sessionRR, 1.0);
        return weightedRR_we;
    }

    private WeightedReferenceResolver getTheyResolver() {
        WeightedReferenceResolver weightedRR_they  = new WeightedReferenceResolver();
        NotSpeakerSessionReferenceResolver notSpeakerSessionRR = new NotSpeakerSessionReferenceResolver(knowledgeBase);
        notSpeakerSessionRR.setSpeakerId(speakerId);

        weightedRR_they.addResolver(new ReverseReferenceResolver(speakerRR), 0.5);
        weightedRR_they.addResolver(notSpeakerSessionRR, 0.5);
        return weightedRR_they;
    }


    private Map<String, WeightedReferenceResolver> getPronoun2ReferenceResolver() {

        Map<String, WeightedReferenceResolver> pronounToRefResolverMap = new HashMap<>();
        speakerRR.setSpeakerId(speakerId);
        sessionRR.setSpeakerId(speakerId);

        pronounToRefResolverMap.put("I", getIResolver());
        pronounToRefResolverMap.put("you", getYouResolver());
        pronounToRefResolverMap.put("he", getGenderResolver("male"));
        pronounToRefResolverMap.put("she", getGenderResolver("female"));
        pronounToRefResolverMap.put("we", getWeResolver());
        pronounToRefResolverMap.put("they", getTheyResolver());

        //TODO different foci

        return pronounToRefResolverMap;
    }
}
