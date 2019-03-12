package de.dfki.step.output;

import de.dfki.step.core.Mode;

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
