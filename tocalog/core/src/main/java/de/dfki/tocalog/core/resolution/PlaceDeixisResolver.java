package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.Hypothesis;
import de.dfki.tocalog.core.Inputs;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.Ontology;

import java.util.Optional;

public class PlaceDeixisResolver {
    private KnowledgeBase kb;

    public PlaceDeixisResolver(KnowledgeBase kb) {
        this.kb = kb;
    }


    public Hypothesis resolve(Hypothesis hypothesis, String slotName, String candidateValue, Inputs inputs, Input lastInput) {
        String speakerString = ((TextInput) lastInput).getSource().get(); //TODO not source
        //find initiator in kb
        Optional<Entity> speaker = kb.getKnowledgeMap(Ontology.Person).getAll().stream()
                .filter(p -> p.get(Ontology.id).get().equals(speakerString))
                .findAny();
        if (!speaker.isPresent()) {
            return hypothesis;
        }

        if (candidateValue.equals("here")) { //"to me" also possible
            // Location location = initiator.getLocation();

        }
        //for "there" check focus or pointing
        return hypothesis;

    }
}
