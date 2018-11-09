package a;

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
    private KnowledgeMap personMap;

    public PersonDeixisResolver(KnowledgeBase kb) {
        this.kb = kb;
        personMap = kb.getKnowledgeMap(Ontology.Person);
    }

   // public Hypothesis resolve(Hypothesis hypothesis, String slotName, String candidateValue, Inputs inputs, Input lastInput)


    public Collection<Entity> resolvePerson(Input lastInput, String slotInput) {
        Collection<Entity> candidates = personMap.getAll();

        //check if persons name is used in input
        Collection<Entity> persons = personMap.query(e -> e.get(Ontology.name).orElse("").equals(slotInput));
        if(persons != null && !persons.isEmpty()) {
            return persons;
        }

        String initiator = lastInput.getInitiator();
        if(slotInput.equals("I") || slotInput.equals("me") || slotInput.equals("my") || slotInput.equals("mine")) {
            candidates = filterSpeaker(candidates, initiator, true);
        }else if(slotInput.equals("you") || slotInput.equals("your") || slotInput.equals("yours")) {
            candidates = filterSpeaker(candidates, initiator, false);
            candidates = filterSession(candidates, initiator, true);
            candidates = filterGaze(candidates, initiator);
        }else if(slotInput.equals("she") || slotInput.equals("her") || slotInput.equals("hers")) {
            candidates = filterGender(candidates, "female");
            candidates = filterSpeaker(candidates, initiator, false);
            if(candidates.size() > 1) {
                candidates = filterSession(candidates,initiator, true);
                if(candidates.size() > 1) {
                    candidates = filterGaze(candidates, initiator);
                }
            }
        }else if(slotInput.equals("he") || slotInput.equals("his")) {
            candidates = filterGender(candidates, "male");
            candidates = filterSpeaker(candidates, initiator, false);
            if(candidates.size() > 1) {
                candidates = filterSession(candidates,initiator, true);
                if(candidates.size() > 1) {
                    candidates = filterGaze(candidates, initiator);
                }
            }
        }else if(slotInput.equals("we") || slotInput.equals("our") || slotInput.equals("ours")) {
            candidates = filterSession(candidates,initiator, true);
        }else if(slotInput.equals("they") || slotInput.equals("their") || slotInput.equals("theirs") || slotInput.equals("them")) {
            candidates = filterSession(candidates,initiator, false);
        }
        return candidates;

    }



    public Collection<Entity> filterSpeaker(Collection<Entity> candidates, String initiator, boolean initiatorWanted) {
        Collection<Entity> newCandidates = candidates;
        if(initiatorWanted) {
            newCandidates = candidates.stream()
                .filter(e -> e.get(Ontology.id).orElse("").equals(initiator))
                .collect(Collectors.toList());
        }else{
            newCandidates = candidates.stream()
                    .filter(e -> !e.get(Ontology.id).orElse("").equals(initiator))
                    .collect(Collectors.toList());
        }
        if(newCandidates.isEmpty()) {
            return candidates;
        }

        return newCandidates;
    }

    public Collection<Entity> filterGender(Collection<Entity> candidates,String gender) {
        Collection<Entity> newCandidates = candidates;
        //can null be returned from stream or just empty collection??
         newCandidates = candidates.stream()
                    .filter(e -> e.get(Ontology.gender).orElse("").equals(gender))
                    .collect(Collectors.toList());
         if(newCandidates.isEmpty()) {
             return candidates;
         }
         return newCandidates;
    }


    public Collection<Entity> filterSession(Collection<Entity> candidates, String initiator, boolean initiatorSession) {
        //KnowledgeMap sessions = kb.getKnowledgeMap(Ontology.Session);
        //get session where speaker is present -> return all people in that session
        return candidates;
    }

    public Collection<Entity> filterGaze(Collection<Entity> candidates, String initiator) {
        return candidates;
    }



}
