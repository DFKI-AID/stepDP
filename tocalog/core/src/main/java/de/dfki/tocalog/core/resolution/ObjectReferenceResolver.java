package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.Hypothesis;
import de.dfki.tocalog.core.Inputs;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.kb.KnowledgeBase;

public class ObjectReferenceResolver {
    private KnowledgeBase kb;

    public ObjectReferenceResolver(KnowledgeBase kb)  {
        this.kb = kb;
    }


    public Hypothesis resolve(Hypothesis hypothesis, String slotName, String candidateValue, Inputs inputs, Input lastInput) {

        //check if candidate value is valid object (could contain attributes like color, size, which can be used to filter objects)

        //check if previous input contains valid entity for slot?

        //check current focus

        //check if pointing input (pointing as input not sensor info?)

        //check if possessive attribute used (e.g. my hammer) -> call person deixis?


        return hypothesis;
    }
}
