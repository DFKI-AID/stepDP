package de.dfki.step.resolution;

import de.dfki.step.deprecated.kb.DataEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/* confidence is given to the session including the speaker */
public class SessionRR implements ReferenceResolver {


    private String speakerId = "";
    private Collection<DataEntry> sessions;

    public SessionRR(Supplier<Collection<DataEntry>> sessionSupplier) {
        sessions = sessionSupplier.get();
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();

        DataEntry speakerSession = null;
        List<DataEntry> otherSessions = new ArrayList<>();

        for(DataEntry s: sessions) {
            if(s.get("agents", Collection.class).isPresent()) {
                if (s.get("agents", Collection.class).get().contains(speakerId)) {
                    speakerSession = s;
                }else {
                    otherSessions.add(s);
                }
            }
        }

        if(speakerSession != null) {
            if (speakerSession.get("agents", Collection.class).isPresent()) {
                // better: take confidence that agent is in session
                for (String a : (Collection<String>) speakerSession.get("agents", Collection.class).get()) {
                    distribution.getConfidences().put(a, 1.0 / speakerSession.get("agents", Collection.class).get().size());
                }
            }
        }

        for(DataEntry s: otherSessions) {
            if(s.get("agents", Collection.class).isPresent()) {
                for (String a : (Collection<String>) s.get("agents", Collection.class).get()) {
                    distribution.getConfidences().put(a, 0.0);
                }
            }
        }

        return distribution;

    }
}
