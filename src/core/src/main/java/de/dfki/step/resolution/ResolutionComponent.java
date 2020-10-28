package de.dfki.step.resolution;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import de.dfki.step.core.InputComponent;
import de.dfki.step.core.Token;
import de.dfki.step.deprecated.kb.DataEntry;
import org.pcollections.HashTreePSet;
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
    private Supplier<Collection<DataEntry>> personSupplier;
    private Supplier<Collection<DataEntry>> physicalEntitySupplier;
    private Supplier<Collection<DataEntry>> sessionSupplier;
    private PSequence<ReferenceDistribution> distributions = TreePVector.empty();
    private final double RESOLUTION_CONFIDENCE = 0.1;
    private PSet<Token> currentTokens = HashTreePSet.empty();


    public void setPersonSupplier(Supplier<Collection<DataEntry>> personSupplier) {
        this.personSupplier = personSupplier;
    }

    public void setPhysicalEntitySupplier(Supplier<Collection<DataEntry>> physicalEntitySupplier) {
        this.physicalEntitySupplier = physicalEntitySupplier;
    }

    public void setSessionSupplier(Supplier<Collection<DataEntry>> sessionSupplier) {
        this.sessionSupplier = sessionSupplier;
    }

    @Override
    public void init(ComponentManager cm) {
        this.cm = cm;
    }

    @Override
    public void deinit() {

    }

    public PSequence<ReferenceDistribution> getReferenceDistribution() {
        return distributions;
    }

    public PSet<Token> getTokens() {
        return currentTokens;
    }

    @Override
    public void update() {
        currentTokens = cm.retrieveComponent(InputComponent.class).getTokens();
        PSet<Token> modifiedTokens = HashTreePSet.empty();
        for(Token token: currentTokens) {
            modifiedTokens = modifiedTokens.plus(doResolution(token));
        }

        synchronized (this) {
            currentTokens = modifiedTokens;
        }
    }


    public Token doResolution(Token token) {
        ReferenceResolver rr = null;

        if(token.has("slots")) {
            //currently only one slot is supported, how to add list in grammar?
            //TODO: support more than one slot
            Map<String,Object> slotinfo = (Map<String, Object>) token.get("slots").get();
            Map<String,Object> slotCopy = new HashMap();
            slotCopy.putAll(slotinfo);
            // for(Map<String, Object> slotinfo: slots) {
                //check if slot has already been resolved in previous iteration
                if(slotinfo.containsKey("resolved")) {
                    if((Boolean) slotinfo.get("resolved")) {
                        return token;
                    }
                }
                ReferenceDistribution distribution;
                if(slotinfo.containsKey("slot_type")) {
                    if(slotinfo.get("slot_type").equals("entity")) {
                        if(slotinfo.get("entity_type").equals("person")) {
                            rr = new PersonRR(personSupplier);
                        }else if(slotinfo.get("entity_type").equals("personal_pronoun")) {
                            rr = new PersonPronounRR(personSupplier, sessionSupplier);
                        }else {
                            rr = new ObjectRR(() -> physicalEntitySupplier.get().stream().filter(o -> o.get("entity_type").get().equals(slotinfo.get("entity_type"))).collect(Collectors.toList()));
                            if(slotinfo.containsKey("personal_pronoun")) {
                                ((ObjectRR) rr).setPronoun((String)slotinfo.get("personal_pronoun"));
                            }
                            if(slotinfo.containsKey("attributes")) {
                                Map<String, Object> attrMap = new HashMap<>();
                                Map<String, Object> attributes = (Map<String, Object>) slotinfo.get("attributes");
                                //for(Map<String, Object> attr: attributes) {
                                    String attrKey = (String) attributes.get("attribute_type");
                                    Object attrValue =  attributes.get("attribute_value");
                                    attrMap.put(attrKey, attrValue);
                               // }
                                ((ObjectRR) rr).setAttrMap(attrMap);

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
                        slotCopy.put("resolved", true);
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
                        slotCopy.put("resolved_candidates", candidates);
                    }else {
                        slotCopy.put("resolved", false);
                    }
                }else {
                    slotCopy.put("resolved", false);
                }
            //}
            token = token.add("slots", slotCopy);
        }

        System.out.println("modified token: " + token.toString());
        return token;
    }

    @Override
    public Object createSnapshot() {
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {

    }

}
