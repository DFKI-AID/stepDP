package de.dfki.tocalog.output;

/**
 */
public class SpeechOutput extends AudioOutput {
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
}
