package de.dfki.tocalog.rasa;

import de.dfki.tocalog.core.*;
import de.dfki.tocalog.core.resolution.ObjectReferenceResolver;
import de.dfki.tocalog.core.resolution.PersonReferenceResolver;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.kb.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class PostRasaHypoProducer implements HypothesisProducer {
    private static final Logger log = LoggerFactory.getLogger(PostRasaHypoProducer.class);
    private final RasaHypoProducer rasaHP;
    private KnowledgeBase knowledgeBase;
    private PersonReferenceResolver personDeixis;
    private ObjectReferenceResolver objectReferenceResolver;

    public PostRasaHypoProducer(RasaHypoProducer rasaHP, KnowledgeBase kb) {
        this.rasaHP = rasaHP;
        this.knowledgeBase = kb;
    }

    @Override
    public List<Hypothesis> process(Inputs inputs) {
       List<Hypothesis> rasaHypos = rasaHP.process(inputs);
       for(Hypothesis rasaHypo: rasaHypos) {
           Map<String, Slot> slotMap = rasaHypo.getSlots();
           String speaker = inputs.getInputs().stream()
                   .filter(i -> i.getId().equals(rasaHypo.getInputs().toArray()[0]))
                   .findFirst().get().getInitiator();
           for(Slot slot: slotMap.values()) {
               if(slot.getCandidates().stream().findAny().isPresent()) {
                   Entity candidateEnt = slot.getCandidates().stream().findAny().get();
                   if(candidateEnt.get(Ontology.type).orElse("").equals("person")) {
                       personDeixis = new PersonReferenceResolver(knowledgeBase, candidateEnt.get(Ontology.name).orElse(""));
                       personDeixis.setSpeakerId(speaker);
                       ReferenceDistribution personDist = personDeixis.getReferences();
                       if(!personDist.getConfidences().isEmpty()) {
                           slot.setCandidateMap(personDist.getConfidences());
                       }
                   }else if(candidateEnt.get(Ontology.type).orElse("").equals("entity")) {
                       objectReferenceResolver = new ObjectReferenceResolver(knowledgeBase, candidateEnt.get(Ontology.name).orElse(""), Ontology.Device); //new Type(candidateEnt.get(Ontology.type).get()));
                       objectReferenceResolver.setSpeakerId(speaker);
                       ReferenceDistribution objectDist = objectReferenceResolver.getReferences();
                       if(!objectDist.getConfidences().isEmpty()) {
                           slot.setCandidateMap(objectDist.getConfidences());
                       }
                   }
               }
           }


       }
       return rasaHypos;
    }



}
