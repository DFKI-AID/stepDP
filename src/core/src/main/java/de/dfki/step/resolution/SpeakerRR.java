package de.dfki.step.resolution;

import de.dfki.step.deprecated.kb.DataEntry;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/* the speaker is resolved */
public class SpeakerRR implements ReferenceResolver {


    private String speakerId = "";
    private Collection<DataEntry> persons;


    public SpeakerRR(Supplier<Collection<DataEntry>> personSupplier) {
        persons = personSupplier.get();
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();

        Collection<DataEntry> speakers = persons.stream()
                .filter(e -> e.getId().equals(speakerId))
                .collect(Collectors.toList());

        Collection<DataEntry> nonspeakers = persons.stream()
                .filter(e -> !e.getId().equals(speakerId))
                .collect(Collectors.toList());

        // 1.0/0.0 could be replaced with speaker confidence and rest
        for(DataEntry s: speakers) {
            distribution.getConfidences().put(s.getId(), 1.0);
        }
        for(DataEntry s: nonspeakers) {
            distribution.getConfidences().put(s.getId(), 0.0);

        }
        return distribution;

    }
}
