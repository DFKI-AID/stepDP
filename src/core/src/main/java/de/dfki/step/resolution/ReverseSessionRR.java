package de.dfki.step.resolution;

import de.dfki.step.kb.DataEntry;
import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/* confidence is given to sessions not including the speaker */
public class ReverseSessionRR implements ReferenceResolver {


    private String speakerId = "";
    private Collection<DataEntry> sessions;

    public ReverseSessionRR(Supplier<Collection<DataEntry>> sessionSupplier) {
        sessions = sessionSupplier.get();
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    @Override
    public ReferenceDistribution getReferences() {
        ReferenceDistribution distribution = new ReferenceDistribution();

        Collection<DataEntry> othersessions = new ArrayList<DataEntry>();
        for(DataEntry s: sessions) {
            if(s.get("agents").isPresent()) {
                if (!s.get("agents", Collection.class).get().contains(speakerId)) {
                    othersessions.add(s);
                }
            }
        }

        for(DataEntry s: othersessions) {
            if(s.get("agents", Collection.class).isPresent()) {
                for (String a : (Collection<String>) s.get("agents", Collection.class).get()) {
                    distribution.getConfidences().put(a, 1.0 / s.get("agents", Collection.class).get().size());
                }
            }
        }


        return distribution;

    }
}
