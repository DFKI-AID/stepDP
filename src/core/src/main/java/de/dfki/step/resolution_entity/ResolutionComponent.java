package de.dfki.step.resolution_entity;

import de.dfki.step.core.*;
import de.dfki.step.deprecated.kb.Attribute;
import de.dfki.step.deprecated.kb.AttributeValue;
import de.dfki.step.deprecated.kb.Entity;
import org.pcollections.PSequence;
import org.pcollections.PSet;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ResolutionComponent implements Component {

    private static Logger log = LoggerFactory.getLogger(ResolutionComponent.class);
    private ComponentManager cm;
    private Supplier<Collection<Entity>> personSupplier;
    private Supplier<Collection<Entity>> physicalEntitySupplier;
    private Supplier<Collection<Entity>> sessionSupplier;
    private PSequence<de.dfki.step.resolution_entity.ReferenceDistribution> distributions = TreePVector.empty();
    private final double RESOLUTION_CONFIDENCE = 0.1;


    public void setPersonSupplier(Supplier<Collection<Entity>> personSupplier) {
        this.personSupplier = personSupplier;
    }

    public void setPhysicalEntitySupplier(Supplier<Collection<Entity>> physicalEntitySupplier) {
        this.physicalEntitySupplier = physicalEntitySupplier;
    }

    public void setSessionSupplier(Supplier<Collection<Entity>> sessionSupplier) {
        this.sessionSupplier = sessionSupplier;
    }

    @Override
    public void init(ComponentManager cm) {
        this.cm = cm;
    }

    @Override
    public void deinit() {

    }

    public PSequence<de.dfki.step.resolution_entity.ReferenceDistribution> getReferenceDistribution() {
        return distributions;
    }

    @Override
    public void update() {
        PSet<Token> tokens = cm.retrieveComponent(InputComponent.class).getTokens();
        for(Token token: tokens) {
            doResolution(token);
        }
    }


    public void doResolution(Token token) {
        System.out.println("in doResolution");
        de.dfki.step.resolution_entity.ReferenceResolver rr = null;

        if(token.has("slots")) {
            List<Map<String,Object>> slots = (List<Map<String, Object>>) token.get("slots").get();
            for(Map<String, Object> slotinfo: slots) {
                //check if slot has already been resolved in previous iteration
                if(slotinfo.containsKey("resolved")) {
                    if((Boolean) slotinfo.get("resolved")) {
                        continue;
                    }
                }
                de.dfki.step.resolution_entity.ReferenceDistribution distribution;
                if(slotinfo.containsKey("slot_type")) {
                    if(slotinfo.get("slot_type").equals("entity")) {
                        if(slotinfo.get("entity_type").equals("person")) {
                            rr = new de.dfki.step.resolution_entity.PersonRR(personSupplier);
                        }else if(slotinfo.get("entity_type").equals("personal_pronoun")) {
                            rr = new de.dfki.step.resolution_entity.PersonPronounRR(personSupplier, sessionSupplier);
                        }else {
                            rr = new de.dfki.step.resolution_entity.ObjectRR(() -> physicalEntitySupplier.get().stream().filter(o -> o.attributes.get("entity_type").equals(slotinfo.get("entity_type"))).collect(Collectors.toList()));
                            if(slotinfo.containsKey("personal_pronoun")) {
                                ((de.dfki.step.resolution_entity.ObjectRR) rr).setPronoun((String)slotinfo.get("personal_pronoun"));
                            }
                            if(slotinfo.containsKey("attributes")) {
                                Map<Attribute, AttributeValue> attrMap = new HashMap<>();
                                List<Map<String, Object>> attributes = (List) slotinfo.get("attributes");
                                for(Map<String, Object> attr: attributes) {
                                    Attribute a = new Attribute((String)attr.get("attribute_type"));
                                    String aValue = (String) attr.get("attribute_value");
                                    attrMap.put(a, new AttributeValue(aValue, aValue, a));
                                }
                                ((de.dfki.step.resolution_entity.ObjectRR) rr).setAttrMap(attrMap);

                            }
                        }
                    }else if(slotinfo.get("slot_type").equals("location")) {
                        //TODO
                    }
                }
                if(rr != null) {
                    distribution = rr.getReferences();
                    if(distribution != null) {
                        Map<String, Double> dCandidates = distribution.getConfidences();
                        slotinfo.put("resolved", true);
                        List<Map<String, Object>> candidates = new ArrayList<>();
                        for(Map.Entry entry: dCandidates.entrySet()) {
                            //only add candidated if probability is above threshold (e.g. 0.1)
                            if((Double) entry.getValue() > RESOLUTION_CONFIDENCE) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("res_id", entry.getKey());
                                map.put("res_conf", entry.getValue());
                                candidates.add(map);
                            }
                        }
                        slotinfo.put("resolved_candidates", candidates);
                    }else {
                        slotinfo.put("resolved", false);
                    }
                }else {
                    slotinfo.put("resolved", false);
                }
            }

            System.out.println("Token in Resolution: " + token.toString());
        }

    }

    @Override
    public Object createSnapshot() {
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {

    }

}
