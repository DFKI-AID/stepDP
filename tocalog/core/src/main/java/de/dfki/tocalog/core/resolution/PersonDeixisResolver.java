package de.dfki.tocalog.core.resolution;

import de.dfki.tocalog.core.Hypothesis;
import de.dfki.tocalog.core.Inputs;
import de.dfki.tocalog.core.Slot;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 */
public class PersonDeixisResolver {
    private KnowledgeBase kb;

    public PersonDeixisResolver(KnowledgeBase kb) {
        this.kb = kb;
    }

    public Hypothesis resolve(Hypothesis hypothesis, String slotName, String candidateValue, Inputs inputs, Input lastInput) {
        Slot slot = hypothesis.getSlot(slotName).get();
        //if candidate is person in kb
        KnowledgeMap personMap = kb.getKnowledgeMap(Ontology.Person);
        Collection<Entity> persons = personMap.query(e -> e.get(Ontology.name).orElse("").equals(candidateValue));

        if (persons.isEmpty()) {
            return hypothesis;
        }

        for (Entity person : personMap.getAll()) {
//
//            if (person.getName().equals(candidateValue)) {
//                Slot.Candidate<Person> candidate = new Slot.Candidate();
//                candidate.setCandidate(person);
//                //  hypothesis.getInputs().add(lastInput.)
//                return hypothesis;
//            }
        }

        //if personal pronoun is used:
        //look for female persons
//        if (candidateValue.equals("her") || candidateValue.equals("hers")) {
//            List<Person> females = personMap.getAll().stream().filter(person -> person.getGender().get().equals("female")).collect(Collectors.toList());
//            //TODO: filter session of initiator
//
//            //TODO: filter Fokus
//
//            //TODO: filter last female person mentioned in previous inputs -> storing last "topic"-focus in kb would be helpful
//
//
//        }

        return hypothesis;
    }


}