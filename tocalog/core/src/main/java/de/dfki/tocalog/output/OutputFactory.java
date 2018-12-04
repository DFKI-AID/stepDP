package de.dfki.tocalog.output;

import de.dfki.tocalog.core.Mode;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.Ontology;

/**
 */
public class OutputFactory {
    public Entity createSpeechOutput(String utterance) {
        return new Entity()
                .set(Ontology.utterance, utterance)
                .set(Ontology.type, "Output")
                .set(Ontology.mode, Mode.Audition)
                .set(Ontology.modality, "speech");
    }

    public Entity createTextOutput(String text) {
        return new Entity()
                .set(Ontology.utterance, text)
                .set(Ontology.type, "Output")
                .set(Ontology.mode, Mode.Vision)
                .set(Ontology.modality, "text");
    }
}
