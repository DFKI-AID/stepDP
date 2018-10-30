package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.Hypothesis;
import de.dfki.tocalog.core.Inputs;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.kb.KnowledgeBase;

public class TimeDeixisResolver {
    private KnowledgeBase kb;

    public TimeDeixisResolver(KnowledgeBase kb)  {
        this.kb = kb;
    }


    public Hypothesis resolve(Hypothesis hypothesis, String slotName, String candidateValue, Inputs inputs, Input lastInput) {

        return hypothesis;
    }
}
