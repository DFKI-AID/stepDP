package de.dfki.step.resolution_entity;

import de.dfki.step.kb.Entity;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/*resolves personal pronouns -> weighted distribution is used, for example speaker, gender and session are considered */
public class PersonPronounRR implements de.dfki.step.resolution_entity.ReferenceResolver {

    private String speakerId = "";
    private String pronoun = "";
    private de.dfki.step.resolution_entity.SpeakerRR speakerRR;
    private de.dfki.step.resolution_entity.SessionRR sessionRR;
    private Supplier<Collection<Entity>> personSupplier;
    private Supplier<Collection<Entity>> sessionSupplier;



    public PersonPronounRR(Supplier<Collection<Entity>> personSupplier, Supplier<Collection<Entity>> sessionSupplier) {
        this.personSupplier = personSupplier;
        this.sessionSupplier = sessionSupplier;
    }


    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }


    public void setPronoun(String pronoun) {
        this.pronoun = pronoun;
    }


    @Override
    public de.dfki.step.resolution_entity.ReferenceDistribution getReferences() {
        this.speakerRR = new de.dfki.step.resolution_entity.SpeakerRR(personSupplier);
        this.sessionRR = new de.dfki.step.resolution_entity.SessionRR(sessionSupplier);
        speakerRR.setSpeakerId(speakerId);
        sessionRR.setSpeakerId(speakerId);

        Map<String, de.dfki.step.resolution_entity.WeightedRR> pronoun2ReferenceResolverMap = getPronoun2ReferenceResolver();
        return pronoun2ReferenceResolverMap.get(pronoun).getReferences();

    }

    private de.dfki.step.resolution_entity.WeightedRR getIResolver() {
        de.dfki.step.resolution_entity.WeightedRR weightedRR_I = new de.dfki.step.resolution_entity.WeightedRR();
        weightedRR_I.addResolver(speakerRR, 1.0);
        return weightedRR_I;
    }

    private de.dfki.step.resolution_entity.WeightedRR getYouResolver() {
        de.dfki.step.resolution_entity.WeightedRR weightedRR_you  = new de.dfki.step.resolution_entity.WeightedRR();
        weightedRR_you.addResolver(new de.dfki.step.resolution_entity.ReverseRR(speakerRR), 0.5);
        weightedRR_you.addResolver(sessionRR, 0.5);
        return weightedRR_you;
    }

    private de.dfki.step.resolution_entity.WeightedRR getGenderResolver(String gender) {
        de.dfki.step.resolution_entity.WeightedRR weightedRR_gender  = new de.dfki.step.resolution_entity.WeightedRR();
        weightedRR_gender.addResolver(new de.dfki.step.resolution_entity.ReverseRR(speakerRR), 0.2);
        de.dfki.step.resolution_entity.GenderRR genderRR = new de.dfki.step.resolution_entity.GenderRR(personSupplier);
        genderRR.setGender(gender);
        weightedRR_gender.addResolver(genderRR, 0.7);
        weightedRR_gender.addResolver(sessionRR, 0.1);
        return weightedRR_gender;
    }


    private de.dfki.step.resolution_entity.WeightedRR getWeResolver() {
        de.dfki.step.resolution_entity.WeightedRR weightedRR_we  = new de.dfki.step.resolution_entity.WeightedRR();
        weightedRR_we.addResolver(sessionRR, 1.0);
        return weightedRR_we;
    }

    private de.dfki.step.resolution_entity.WeightedRR getTheyResolver() {
        de.dfki.step.resolution_entity.WeightedRR weightedRR_they  = new de.dfki.step.resolution_entity.WeightedRR();
        de.dfki.step.resolution_entity.ReverseSessionRR otherSessionRR = new de.dfki.step.resolution_entity.ReverseSessionRR(sessionSupplier);
        otherSessionRR.setSpeakerId(speakerId);

        weightedRR_they.addResolver(new de.dfki.step.resolution_entity.ReverseRR(speakerRR), 0.5);
        weightedRR_they.addResolver(otherSessionRR, 0.5);
        return weightedRR_they;
    }


    private Map<String, de.dfki.step.resolution_entity.WeightedRR> getPronoun2ReferenceResolver() {

        Map<String, de.dfki.step.resolution_entity.WeightedRR> pronounToRefResolverMap = new HashMap<>();
        speakerRR.setSpeakerId(speakerId);
        sessionRR.setSpeakerId(speakerId);

        pronounToRefResolverMap.put("i", getIResolver());
        pronounToRefResolverMap.put("you", getYouResolver());
        pronounToRefResolverMap.put("he", getGenderResolver("male"));
        pronounToRefResolverMap.put("she", getGenderResolver("female"));
        pronounToRefResolverMap.put("we", getWeResolver());
        pronounToRefResolverMap.put("they", getTheyResolver());



        return pronounToRefResolverMap;
    }


}
