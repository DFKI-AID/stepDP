package a;

import de.dfki.tocalog.core.*;
import de.dfki.tocalog.core.Slot;
import de.dfki.tocalog.core.resolution.ObjectReferenceResolver;
import de.dfki.tocalog.core.resolution.PersonReferenceResolver;
import de.dfki.tocalog.kb.*;
import de.dfki.tocalog.rasa.RasaHypoProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/*
* postprocesses the rasa hypos by resolving references
* assuming that certain types of slots are present and have been correctly recognized by rasa,
* properties are defined in extra slot before entity
*
*/
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
           Map<String, de.dfki.tocalog.core.Slot> slotMap = rasaHypo.getSlots();
           String speaker = inputs.getInputs().stream()
                   .filter(i -> i.getId().equals(rasaHypo.getInputs().toArray()[0]))
                   .findFirst().get().getInitiator();

           int idx = 0;
           List<de.dfki.tocalog.core.Slot> slotList = arrangeSlotOrder(slotMap.values());
           for(de.dfki.tocalog.core.Slot slot: slotList) {
               if(!slot.getCandidates().stream().findAny().isPresent()) {
                   continue;
               }
               Entity candidateEnt = slot.getCandidates().stream().findAny().get();

               if(candidateEnt.get(Ontology.type).orElse("").equals("person")) {
              //     personDeixis = new PersonReferenceResolver(knowledgeBase, candidateEnt.get(Ontology.name).orElse(""));
                   personDeixis.setSpeakerId(speaker);
                   ReferenceDistribution personDist = personDeixis.getReferences();
                   if(!personDist.getConfidences().isEmpty()) {
                //       slot.setCandidateMap(personDist.getConfidences());
                   }

               }else if(candidateEnt.get(Ontology.type).orElse("").equals("device")) {
              //     objectReferenceResolver = new ObjectReferenceResolver(knowledgeBase, candidateEnt.get(Ontology.name).orElse(""), Ontology.Device); //new Type(candidateEnt.get(Ontology.type).get()));
                   objectReferenceResolver.setSpeakerId(speaker);

                   Map<Attribute, AttributeValue> attributeMap = new HashMap<>();
                   for(int i = idx-1; i >= 0; i--) {
                       if(!slotList.get(i).getCandidates().stream().findAny().isPresent()) {
                           continue;
                       }
                       Entity attributeEnt = slotList.get(i).getCandidates().stream().findAny().get();
                       if(attributeEnt.get(Ontology.type).orElse("").equals("color")) {
                           AttributeValue attrVal = new AttributeValue<>(Ontology.color.name, attributeEnt.get(Ontology.name).get(), Ontology.color);
                           attributeMap.put(Ontology.color, attrVal);
                       } else if(attributeEnt.get(Ontology.type).orElse("").equals("size")) {
                           AttributeValue attrVal = new AttributeValue<>(Ontology.size.name, attributeEnt.get(Ontology.name).get(), Ontology.size);
                           attributeMap.put(Ontology.size,attrVal);
                       }else {
                           break;
                       }
                   }
                   objectReferenceResolver.setAttrMap(attributeMap);

                   ReferenceDistribution objectDist = objectReferenceResolver.getReferences();
                   if(!objectDist.getConfidences().isEmpty()) {
                  //     slot.setCandidateMap(objectDist.getConfidences());
                   }

               }
               idx++;
           }


       }
       return rasaHypos;
    }


    public List<de.dfki.tocalog.core.Slot> arrangeSlotOrder(Collection<de.dfki.tocalog.core.Slot> slots) {
        Map<de.dfki.tocalog.core.Slot, Integer> slotPosMap = new HashMap<>();
        for(de.dfki.tocalog.core.Slot slot: slots) {
            if (slot.getCandidates().stream().findAny().isPresent()) {
                Entity candidateEnt = slot.getCandidates().stream().findAny().get();
                String[] positions = slot.name.split(candidateEnt.get(Ontology.type).get())[1].split("-");
                slotPosMap.put(slot, Integer.parseInt(positions[0]));

            }
        }
        List<Map.Entry<de.dfki.tocalog.core.Slot, Integer>> sortedSlots = new ArrayList<>(slotPosMap.entrySet());
        sortedSlots.sort(Map.Entry.comparingByValue());

        List<de.dfki.tocalog.core.Slot> sortedSlotList = new ArrayList<>();
        for (Map.Entry<Slot, Integer> entry : sortedSlots) {
            sortedSlotList.add(entry.getKey());
        }
        System.out.println("sortedSlotList "  + sortedSlotList.toString());
        return sortedSlotList;
    }






}
