package de.dfki.tocalog.rasa;

import de.dfki.tocalog.core.*;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Uses rasa to parse text / speech / ... inputs into hypothesis with the rasa intent.
 * Values for slots will be stored in the attribute {@link de.dfki.tocalog.kb.Ontology#name}
 * Confidence for slots will be stored in the attribute {@link de.dfki.tocalog.kb.Ontology#confidence}
 *
 * TODO could also concat multiple inputs to derive different intent
 * TODO parse inputs other than TextInput
 */
public class RasaHypoProducer implements HypothesisProducer {
    private static final Logger log = LoggerFactory.getLogger(RasaHypoProducer.class);
    private final RasaHelper rasaHelper;

    public RasaHypoProducer(RasaHelper rasaHelper) {
        this.rasaHelper = rasaHelper;
    }

    @Override
    public List<Hypothesis> process(Inputs inputs) {
        //TODO maybe discard input if multiple rasa intents have similar confidence

        List<Hypothesis> rsps = inputs.getInputs().stream()
                .filter(i -> !inputs.isConsumed(i))
                .filter(i -> i instanceof TextInput)
                .map(i -> (TextInput) i)
                .map(ti -> Pair.of(ti, nlu(ti.getText())))
                .filter(p -> p.second.isPresent())
                .map(p -> Pair.of(p.first, p.second.get()))
                .sorted((o1, o2) -> Double.compare(o2.second.getIntent().getConfidence(), o1.second.getIntent().getConfidence()))
                .map(p -> Pair.of(p.first, parse(p.second)))
                .map(p -> p.second.addInput(p.first).build())
                .collect(Collectors.toList());
        return rsps;
    }

    protected Hypothesis.Builder parse(RasaResponse rsp) {
        Hypothesis.Builder hb = Hypothesis.create(rsp.getIntent().getName());
        rsp.getEntities().forEach(re -> hb.addSlot(parse(re)));
        hb.setConfidence(new Confidence(rsp.getIntent().getConfidence())); //TODO is intent + confidence always set?
        return hb;
    }

    protected Slot parse(RasaEntity re) {
        //TODO case: multiple slot with the same entity? slot would be lost

        return new Slot(re.getEntity() + re.getStart() + re.getEnd()) {
            @Override
            public Collection<Entity> getCandidates() {
                Entity entity = new Entity()
                        .set(Ontology.name, re.getValue())
                        .set(Ontology.type, re.getEntity())
                        .set(Ontology.confidence, re.getConfidence());
                return List.of(entity);
            }
        };
    }


    protected Optional<RasaResponse> nlu(String s) {
        try {
            String rsp = rasaHelper.nlu(s);
            RasaResponse rasaRsp = rasaHelper.parseJson(rsp);
            if(rasaRsp.getIntent() == null || rasaRsp.getIntent().getName() == null) {
                return Optional.empty();
            }
            return Optional.of(rasaRsp);
        } catch (IOException e) {
            log.warn("could not get response from rasa: {}", s);
            return Optional.empty();
        }
    }

    private static class Pair<X, Y> {
        public final X first;
        public final Y second;

        public Pair(X first, Y second) {
            this.first = first;
            this.second = second;
        }

        public static <X, Y> Pair<X, Y> of(X first, Y second) {
            return new Pair<>(first, second);
        }
    }
}
