package de.dfki.step.resolution;

import de.dfki.step.kb.DataEntry;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/*resolves personal pronouns -> weighted distribution is used, for example speaker, gender and session are considered */
public class PersonPronounRR implements ReferenceResolver {

    private String speakerId = "";
    private String pronoun = "";
    private SpeakerRR speakerRR;
    private SessionRR sessionRR;
    private Supplier<Collection<DataEntry>> personSupplier;
    private Supplier<Collection<DataEntry>> sessionSupplier;



    public PersonPronounRR(Supplier<Collection<DataEntry>> personSupplier, Supplier<Collection<DataEntry>> sessionSupplier) {
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
    public ReferenceDistribution getReferences() {
        this.speakerRR = new SpeakerRR(personSupplier);
        this.sessionRR = new SessionRR(sessionSupplier);
        speakerRR.setSpeakerId(speakerId);
        sessionRR.setSpeakerId(speakerId);

        Map<String, WeightedRR> pronoun2ReferenceResolverMap = getPronoun2ReferenceResolver();
        return pronoun2ReferenceResolverMap.get(pronoun).getReferences();

    }

    private WeightedRR getIResolver() {
        WeightedRR weightedRR_I = new WeightedRR();
        weightedRR_I.addResolver(speakerRR, 1.0);
        return weightedRR_I;
    }

    private WeightedRR getYouResolver() {
        WeightedRR weightedRR_you  = new WeightedRR();
        weightedRR_you.addResolver(new ReverseRR(speakerRR), 0.5);
        weightedRR_you.addResolver(sessionRR, 0.5);
        return weightedRR_you;
    }

    private WeightedRR getGenderResolver(String gender) {
        WeightedRR weightedRR_gender  = new WeightedRR();
        weightedRR_gender.addResolver(new ReverseRR(speakerRR), 0.2);
        GenderRR genderRR = new GenderRR(personSupplier);
        genderRR.setGender(gender);
        weightedRR_gender.addResolver(genderRR, 0.7);
        weightedRR_gender.addResolver(sessionRR, 0.1);
        return weightedRR_gender;
    }


    private WeightedRR getWeResolver() {
        WeightedRR weightedRR_we  = new WeightedRR();
        weightedRR_we.addResolver(sessionRR, 1.0);
        return weightedRR_we;
    }

    private WeightedRR getTheyResolver() {
        WeightedRR weightedRR_they  = new WeightedRR();
        ReverseSessionRR otherSessionRR = new ReverseSessionRR(sessionSupplier);
        otherSessionRR.setSpeakerId(speakerId);

        weightedRR_they.addResolver(new ReverseRR(speakerRR), 0.5);
        weightedRR_they.addResolver(otherSessionRR, 0.5);
        return weightedRR_they;
    }


    private Map<String, WeightedRR> getPronoun2ReferenceResolver() {

        Map<String, WeightedRR> pronounToRefResolverMap = new HashMap<>();
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
