package de.dfki.tocalog.rasa;

import de.dfki.tocalog.core.HypothesisProcessor;
import de.dfki.tocalog.core.Hypothesis;
import de.dfki.tocalog.core.ReferenceDistribution;
import de.dfki.tocalog.core.ReferenceResolver;
import de.dfki.tocalog.core.Slot;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RasaHypoProcessor implements HypothesisProcessor {
    private static final Logger log = LoggerFactory.getLogger(RasaHypoProducer.class);
    private final RasaHelper rasaHelper;
    private List<ReferenceResolver> referenceResolvers = new ArrayList<>();
    private List<String> modifiers = new ArrayList<>(List.of("possessive", "color", "size"));

    public RasaHypoProcessor(RasaHelper helper) {
        this.rasaHelper = helper;
    }

    @Override
    public Hypothesis process(Input input, Hypothesis hypothesis) {
        if(!(input instanceof TextInput)) {
            hypothesis.addMatch(RasaHypoProcessor.class, false);
            return hypothesis;
        }
        Optional<RasaResponse> rasaResponse = nlu(((TextInput) input).getText());
        if(!rasaResponse.isPresent()) {
            hypothesis.addMatch(RasaHypoProcessor.class, false);
            return hypothesis;
        }

        if(!rasaResponse.get().getIntent().getName().equals(hypothesis.getIntent())) {
            hypothesis.addMatch(RasaHypoProcessor.class, false);
            return hypothesis;
        }

        hypothesis.addMatch(RasaHypoProcessor.class, true);
            //TODO add input to hypothesis
         //   Hypothesis rasaHypo = new Hypothesis.Builder(hypothesis).build();


        for(RasaEntity rasaEntity: rasaResponse.get().getEntities()) {
            for(Slot slot: hypothesis.getSlots().values()) {
                if (slot.getSlotConstraint().isPresent()) {
                    if(slot.getSlotConstraint().get().validateType(rasaEntity.getEntity())) {
                        Entity candidate = new Entity()
                                .set(Ontology.name, rasaEntity.getValue())
                                .set(Ontology.type, rasaEntity.getEntity())
                                .set(Ontology.confidence, rasaEntity.getConfidence())
                                .set(Ontology.source, this.toString());
                        if(slot.getSlotConstraint().get().validateCandidate(candidate)) {
                            slot.addCandidate(candidate);
                        }else {
                            for(ReferenceResolver resolver: referenceResolvers) {
                                ReferenceDistribution rd = resolver.getReferences();
                                for(Map.Entry<String, Double> e: rd.getConfidences().entrySet()) {
                                    slot.addCandidate(new Entity()
                                            .set(Ontology.name, e.getKey())
                                            .set(Ontology.type, rasaEntity.getEntity())
                                            .set(Ontology.confidence, e.getValue())
                                            .set(Ontology.source, this.toString()));
                                }
                            }
                        }

                    }
                }
            }
        }



        return hypothesis;
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
}
