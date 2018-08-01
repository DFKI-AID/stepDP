package de.dfki.tocalog.output;

import de.dfki.tocalog.model.Mode;

/**
 */
public class SpeechOutput implements Output {
    private String utterance;

    public SpeechOutput(String utterance) {
        this.utterance = utterance;
    }

    @Override
    public String toString() {
        return "SpeechOutput{" +
                "utterance='" + utterance + '\'' +
                '}';
    }

    @Override
    public Mode getMode() {
        return Mode.Audition;
    }
}
