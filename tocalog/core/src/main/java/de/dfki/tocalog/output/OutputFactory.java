package de.dfki.tocalog.output;

import de.dfki.tocalog.core.Mode;
import de.dfki.tocalog.kb.Attribute;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.Ontology;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
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

    public static final Ontology.Scheme ImageOutputScheme = Ontology.AbsScheme.builder()
            .present(Ontology.uri)
            .equal(Ontology.modality, "image")
            .build();

    public static final Ontology.Scheme TextOutputScheme = Ontology.AbsScheme.builder()
            .present(Ontology.utterance)
            .equal(Ontology.modality, "text")
            .build();

    public Entity createTTSOutput(String utterance) {
        return new Entity()
                .set(Ontology.id, "tts" + randomId())
                .set(Ontology.utterance, utterance)
                .set(Ontology.type, "Output")
                .set(Ontology.mode, Mode.Audition)
                .set(Ontology.modality, "speech");
    }

    public Entity createFileOutput(String file) {
        return new Entity()
                .set(Ontology.id, "file" + randomId())
                .set(Ontology.file, file)
                .set(Ontology.type, "Output")
                .set(Ontology.mode, Mode.Audition);
    }


    public Entity createTextOutput(String text) {
        return new Entity()
                .set(Ontology.id, "text" + randomId())
                .set(Ontology.utterance, text)
                .set(Ontology.type, "Output")
                .set(Ontology.mode, Mode.Vision)
                .set(Ontology.modality, "text");
    }

    public Entity createImageOutput(URI uri) {
        return new Entity()
                .set(Ontology.id, "image" + randomId())
                .set(Ontology.uri, uri)
                .set(Ontology.type2, Ontology.Output)
                .set(Ontology.mode, Mode.Vision)
                .set(Ontology.modality, "image");
    }

    private static String randomId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

