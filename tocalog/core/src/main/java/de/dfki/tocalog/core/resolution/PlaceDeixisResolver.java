package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.Hypothesis;
import de.dfki.tocalog.core.Inputs;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.EKnowledgeMap;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.model.Person;

public class PlaceDeixisResolver {
    private KnowledgeBase kb;

    public PlaceDeixisResolver(KnowledgeBase kb)  {
        this.kb = kb;
    }


    public Hypothesis resolve(Hypothesis hypothesis, String slotName, String candidateValue, Inputs inputs, Input lastInput) {
        String speakerString = ((TextInput) lastInput).getSource().get();
        //find speaker in kb
        Person speaker = kb.getKnowledgeMap(Person.class).getAll().stream().filter(person -> person.getName().equals(speakerString)).findAny().get();
        if(candidateValue.equals("here")) { //"to me" also possible
           // Location location = speaker.getLocation();

        }
        //for "there" check focus or pointing
        return hypothesis;

    }
}
