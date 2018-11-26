package de.dfki.tocalog.rasa;


import de.dfki.tocalog.core.*;
import de.dfki.tocalog.core.resolution.LocationReferenceResolver;
import de.dfki.tocalog.core.resolution.ObjectReferenceResolver;
import de.dfki.tocalog.core.resolution.PersonReferenceResolver;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.kb.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Uses rasa to parse text / speech / ... inputs into hypothesis with the rasa intent.
 * Values for slots will be stored in the attribute {@link Ontology#name}
 * Confidence for slots will be stored in the attribute {@link Ontology#confidence}
 *
 * TODO could also concat multiple inputs to derive different intent
 * TODO parse inputs other than TextInput
 */
public class RasaHypoProducer2 implements HypothesisProducer {
    private static final Logger log = LoggerFactory.getLogger(RasaHypoProducer2.class);
    private final RasaHelper rasaHelper;
    private KnowledgeBase knowledgeBase;
    private PersonReferenceResolver personDeixis;
    private ObjectReferenceResolver objectReferenceResolver;

    public RasaHypoProducer2(RasaHelper rasaHelper, KnowledgeBase knowledgeBase) {
        this.rasaHelper = rasaHelper;
        this.knowledgeBase = knowledgeBase;
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
                .map(p -> Pair.of(p.first, parse(inputs.getInputs().get(inputs.getInputs().size()-1), p.second)))
                .map(p -> p.second.addInput(p.first).build())
                .collect(Collectors.toList());
        return rsps;
    }

    protected Hypothesis.Builder parse(Input lastInput, RasaResponse rsp) {
        Hypothesis.Builder hb = Hypothesis.create(rsp.getIntent().getName());

   /*     Optional<RasaEntity> entity = rsp.getEntities().stream().filter(e -> e.getEntity().equals("locationRef")).findAny();
        if(entity.isPresent()) {
            Optional<RasaEntity> previousEntity = rsp.getEntities().stream().filter(e -> e.getEnd() == entity.get().getStart()-1).findAny();
            Optional<RasaEntity> pastEntity = rsp.getEntities().stream().filter(e -> e.getStart() == entity.get().getEnd()+1).findAny();
            if(previousEntity.isPresent() && pastEntity.isPresent()) {
                LocationReferenceResolver locRefResolver = new LocationReferenceResolver(knowledgeBase, previousEntity.get().getEntity(), pastEntity.get().getEntity(), entity.get().getValue());
                locRefResolver.getReferences();
            }
        }*/

        rsp.getEntities().forEach(re -> hb.addSlot(parse(lastInput, re)));
        hb.setConfidence(new Confidence(rsp.getIntent().getConfidence())); //TODO is intent + confidence always set?
        return hb;
    }

    protected Slot parse(Input lastInput, RasaEntity re) {
        //TODO case: multiple slot with the same entity? slot would be lost
        Slot slot = new Slot(re.getEntity());
        if(re.getEntity().equals("person")) {
            personDeixis = new PersonReferenceResolver(knowledgeBase, re.getValue());
            personDeixis.setSpeakerId(lastInput.getInitiator());
            ReferenceDistribution personDist = personDeixis.getReferences();
            if(personDist.getConfidences().isEmpty()) {
                slot.setCandidateMap(Map.of(re.getValue(), re.getConfidence()));
            }else {
                slot.setCandidateMap(personDist.getConfidences());
            }

        }else if(re.getEntity().equals("entity")) {
         //   objectReferenceResolver = new ObjectReferenceResolver(knowledgeBase, re.getValue(), new Type(re.getEntity()));
            objectReferenceResolver = new ObjectReferenceResolver(knowledgeBase, re.getValue(), Ontology.Device);
            objectReferenceResolver.setSpeakerId(lastInput.getInitiator());
            ReferenceDistribution objectDist = objectReferenceResolver.getReferences();
            if(objectDist.getConfidences().isEmpty()) {
                slot.setCandidateMap(Map.of(re.getValue(), re.getConfidence()));
            }else {
                slot.setCandidateMap(objectDist.getConfidences());
            }
        }
        return slot;
       /* return new Slot(re.getEntity()) {
            @Override
            public Collection<Entity> getCandidates() {
                Entity entity = new Entity()
                        .set(Ontology.name, re.getValue())
                        .set(Ontology.type, re.getEntity())
                        .set(Ontology.confidence, re.getConfidence());
                return List.of(entity);
            }
        };*/
    }


    protected Optional<RasaResponse> nlu(String s) {
        try {
            String rsp = rasaHelper.nlu(s);
            RasaResponse rasaRsp = rasaHelper.parseJson(rsp);
            System.out.println(Optional.of(rasaRsp));
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
