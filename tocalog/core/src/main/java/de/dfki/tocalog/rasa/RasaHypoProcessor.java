package de.dfki.tocalog.rasa;

import de.dfki.tocalog.core.*;
import de.dfki.tocalog.core.resolution.AbstractReferenceResolver;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
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
    private KnowledgeBase kb;

    public RasaHypoProcessor(KnowledgeBase kb, RasaHelper helper) {
        this.kb = kb;
        this.rasaHelper = helper;
    }

    public List<ReferenceResolver> getReferenceResolvers() {
        return referenceResolvers;
    }

    public void setReferenceResolvers(List<ReferenceResolver> referenceResolvers) {
        this.referenceResolvers = referenceResolvers;
    }


    @Override
    public void process(Input input, Hypothesis hypothesis) {
        if(!(input instanceof TextInput)) {
           // hypothesis.addMatch(RasaHypoProcessor.class, new Confidence(0.0));
            return;
        }
        Optional<RasaResponse> rasaResponse = nlu(((TextInput) input).getText());

        if(!rasaResponse.isPresent()) {
           // hypothesis.addMatch(RasaHypoProcessor.class, new Confidence(0.0));
            System.out.println("not matched");
            return;
        }

        if(!rasaResponse.get().getIntent().getName().equals(hypothesis.getIntent())) {
           // hypothesis.addMatch(RasaHypoProcessor.class, new Confidence(0.0));
            System.out.println("not matched");
            return;
        }

        System.out.println("rasaResponse: " + rasaResponse.toString());
        hypothesis.addMatch(RasaHypoProcessor.class, new Confidence(rasaResponse.get().getIntent().getConfidence()));
            //TODO add input to hypothesis
         //   Hypothesis rasaHypo = new Hypothesis.Builder(hypothesis).build();

        for(RasaEntity rasaEntity: rasaResponse.get().getEntities()) {
            for(Slot slot: hypothesis.getSlots().values()) {
                if (slot.getSlotConstraint().isPresent()) {
                    //check valid candidate
                    if(!(slot.getSlotConstraint().get() instanceof Slot.SlotTypeConstraint)) {
                        Entity candidate = new Entity()
                                .set(Ontology.name, rasaEntity.getValue())
                                .set(Ontology.type, rasaEntity.getEntity())
                                .set(Ontology.confidence, rasaEntity.getConfidence())
                                .set(Ontology.source, this.toString());
                        if(slot.getSlotConstraint().get().validateCandidate(candidate)) {
                            slot.addCandidate(candidate);
                        }
                        //check valid type and then resolve
                    }else{
                        resolveSlotValue(slot, (TextInput) input, rasaEntity);
                    }
                }
            }
        }


    }


    private void resolveSlotValue(Slot slot, TextInput input, RasaEntity rasaEntity) {
        if(((Slot.SlotTypeConstraint) slot.getSlotConstraint().get()).validateType(rasaEntity.getEntity())) {
            for(ReferenceResolver resolver: referenceResolvers) {
                if(resolver instanceof AbstractReferenceResolver) {
                    ((AbstractReferenceResolver) resolver).setSpeakerId(input.getInitiator());
                    ((AbstractReferenceResolver) resolver).setEntityType(rasaEntity.getEntity());
                    ((AbstractReferenceResolver) resolver).setInputString(rasaEntity.getValue());
                }

                ReferenceDistribution rd = resolver.getReferences();

                for(Map.Entry<String, Double> e: rd.getConfidences().entrySet()) {
                    Entity knowledgeEntity = kb.getKnowledgeMap(rasaEntity.getEntity()).get(e.getKey()).get();
                    slot.addCandidate(knowledgeEntity.set(Ontology.confidence, e.getValue()).set(Ontology.source, this.toString()));
                   /* slot.addCandidate(new Entity()
                            .set(Ontology.name, e.getKey())
                            .set(Ontology.type, rasaEntity.getEntity())
                            .set(Ontology.confidence, e.getValue())
                            .set(Ontology.source, this.toString()));*/
                }
            }

        }
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
