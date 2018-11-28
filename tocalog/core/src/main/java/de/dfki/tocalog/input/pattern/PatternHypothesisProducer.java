package de.dfki.tocalog.input.pattern;

import de.dfki.tocalog.core.*;
import de.dfki.tocalog.core.resolution.ObjectReferenceResolver;
import de.dfki.tocalog.core.resolution.PersonReferenceResolver;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.*;
import de.dfki.tocalog.rasa.RasaHypoProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PatternHypothesisProducer implements HypothesisProducer {
    private static final Logger log = LoggerFactory.getLogger(PatternHypothesisProducer.class);
    private KnowledgeBase knowledgeBase;
    private PersonReferenceResolver personDeixis;
    private ObjectReferenceResolver objectReferenceResolver;
    private List<InputPattern> inputPatterns;

    private static final List<String> stopWordList ;
    static {
        List<String> list = new ArrayList<>();
        list.add("the");
        list.add("a");
        list.add("an");
        list.add("to");
        stopWordList = Collections.unmodifiableList(list);
    }

    public PatternHypothesisProducer(KnowledgeBase knowledgeBase, List<InputPattern> inputPatterns) {
        this.knowledgeBase = knowledgeBase;
        this.inputPatterns = inputPatterns;
    }

    @Override
    public List<Hypothesis> process(Inputs inputs) {
        List<Hypothesis> hypotheses = new ArrayList<>();
        for(Input input: inputs.getInputs()) {
            if(!(input instanceof TextInput)) {
                continue;
            }
            String inputString = ((TextInput) input).getText();
            List<InputPattern> candidatePattern = new ArrayList<>();
            for(InputPattern pattern: inputPatterns) {
                if(inputString.toLowerCase().contains(pattern.getIntent())) {
                    candidatePattern.add(pattern);
                }
            }

            for(InputPattern candidate: candidatePattern) {
                String[] inputSegments = inputString.split(" ");
                Collection<String> inputTokens = List.of(inputSegments).stream().filter(token -> !stopWordList.contains(token) && !token.equalsIgnoreCase(candidate.getIntent())).collect(Collectors.toList());
                if(inputTokens.size() == candidate.getSlotTypes().size()) {
                    double conf = 0.0;
                    Hypothesis.Builder hb = Hypothesis.create(candidate.getIntent());
                    for(int i = 0; i < candidate.getSlotTypes().size(); i++) {
                        //TODO: properties are not handeled right now (Part of Speech tagging to find out adj?)
                        Slot slot = parse(candidate.getSlotTypes().get(i), ((List<String>) inputTokens).get(i), input.getInitiator(), i);
                        hb.addSlot(slot);
                        Optional<Double> slotConf = slot.getCandidates().stream().map(c -> c.get(Ontology.confidence).get()).max(Comparator.naturalOrder());
                        if(slotConf.isPresent()) {
                            conf += slotConf.get();
                        }

                    }
                    hb.setConfidence(new Confidence(conf/inputTokens.size()));
                    hypotheses.add(hb.build());

                }
            }
        }
        return hypotheses;
    }




    public Slot parse(Type type, String value, String speaker, int i) {
        Slot slot = new Slot(type.getName() + i);
        Collection<Entity> candidates = new ArrayList<>();
        ReferenceDistribution distribution = new ReferenceDistribution();

        if(type.equals(Ontology.Person)) {
            personDeixis = new PersonReferenceResolver(knowledgeBase, value);
            personDeixis.setSpeakerId(speaker);
            distribution = personDeixis.getReferences();

            //TODO: check if value string contains a subType of type
        }else if(type.equals(Ontology.Entity) ) {
            objectReferenceResolver = new ObjectReferenceResolver(knowledgeBase, value, type);
            objectReferenceResolver.setSpeakerId(speaker);
            distribution = objectReferenceResolver.getReferences();
        }

        if(!distribution.getConfidences().isEmpty()) {
            for(String id: distribution.getConfidences().keySet()) {
                candidates.add(new Entity()
                        .set(Ontology.name, id)
                        .set(Ontology.type, type.getName())
                        .set(Ontology.confidence, distribution.getConfidences().get(id)));
            }
            slot.setCandidates(candidates);
        }
        return slot;
    }



}
