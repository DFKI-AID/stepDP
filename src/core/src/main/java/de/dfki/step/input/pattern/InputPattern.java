package de.dfki.step.input.pattern;

import de.dfki.step.kb.Type;

import java.util.ArrayList;
import java.util.List;

public class InputPattern {

    /*
    Instruct:   INT OBJ
                INT PERS
                INT LOC
                INT OBJ PERS z.b. Give the book to Magdalena
                INT PERS OBJ
                INT OBJ LOC
                INT PERS LOC
                INT LOC PERS


    SOCIAL: INT_GREET PERS
            INT_AGREE
            INT_DISAGREE
            INT_THANK
            INT_APPOLOGIZE


        PREP* OBJ
        PREP* PERS
        OBJ PREP_LOC
        PERS PREP_LOC
     */

    private String intent;
    private List<Type> slotTypes = new ArrayList<>();


    public InputPattern(String intent) {
        this.intent = intent;
    }

    public InputPattern(String intent, List<Type> slotTypes) {
        this.intent = intent;
    }

    public String getIntent() {
        return intent;
    }

    public List<Type> getSlotTypes() {
        return slotTypes;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setSlotTypes(List<Type> slotTypes) {
        this.slotTypes = slotTypes;
    }
}
