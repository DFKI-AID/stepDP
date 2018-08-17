package de.dfki.tocalog.dialog;

import de.dfki.tocalog.model.Confidence;

import java.util.Optional;

/**
 */
public class Intent {
    private CommunicativeFunction communicativeFunction;
    private String type;
    private String nominative; //wer oder was
    private String genetive; //wessen
    private String dative; //wem
    private String accusative; //wen
    private Optional<Confidence> confidence = Optional.empty();

    public enum CommunicativeFunction {
        Question,
        Statement
    }

    public Intent(CommunicativeFunction communicativeFunction, String intent) {
        this.communicativeFunction = communicativeFunction;
        this.type = intent;
    }

    public String getNominative() {
        return nominative;
    }

    public void setNominative(String nominative) {
        this.nominative = nominative;
    }

    public String getGenetive() {
        return genetive;
    }

    public void setGenetive(String genetive) {
        this.genetive = genetive;
    }

    public String getDative() {
        return dative;
    }

    public void setDative(String dative) {
        this.dative = dative;
    }

    public String getAccusative() {
        return accusative;
    }

    public void setAccusative(String accusative) {
        this.accusative = accusative;
    }

    public CommunicativeFunction getCommunicativeFunction() {
        return communicativeFunction;
    }

    public String getType() {
        return type;
    }

    public Optional<Confidence> getConfidence() {
        return confidence;
    }

    public void setConfidence(Optional<Confidence> confidence) {
        this.confidence = confidence;
    }
}
