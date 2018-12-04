package de.dfki.tocalog.output;

import de.dfki.tocalog.core.Mode;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.Ontology;

import java.util.Objects;
import java.util.UUID;

/**
 */
public class OutputFactory {
    public static final Ontology.AbsScheme AudioOutputScheme = Ontology.AbsScheme.builder()
            .present(Ontology.id)
            .equal(Ontology.type, "Output")
            .equal(Ontology.mode, Mode.Audition)
            .build();

    public static final Ontology.Scheme TTSOutputScheme = AudioOutputScheme.extend()
            .equal(Ontology.modality, "speech")
            .present(Ontology.utterance)
            .build();

    public static final Ontology.Scheme FileOutputScheme = AudioOutputScheme.extend()
            .present(Ontology.file)
            .build();

    public Entity createTTSOutput(String utterance) {
        return new Entity()
                .set(Ontology.id, randomId())
                .set(Ontology.utterance, utterance)
                .set(Ontology.type, "Output")
                .set(Ontology.mode, Mode.Audition)
                .set(Ontology.modality, "speech");
    }

    public Entity createFileOutput(String file) {
        return new Entity()
                .set(Ontology.id, randomId())
                .set(Ontology.file, file)
                .set(Ontology.type, "Output")
                .set(Ontology.mode, Mode.Audition);
    }


    public Entity createTextOutput(String text) {
        return new Entity()
                .set(Ontology.id, randomId())
                .set(Ontology.utterance, text)
                .set(Ontology.type, "Output")
                .set(Ontology.mode, Mode.Vision)
                .set(Ontology.modality, "text");
    }

    private static String randomId() {
        return UUID.randomUUID().toString().substring(0,8);
    }
}

