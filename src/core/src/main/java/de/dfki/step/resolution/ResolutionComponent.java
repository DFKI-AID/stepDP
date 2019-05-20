package de.dfki.step.resolution;

import de.dfki.step.core.*;
import de.dfki.step.kb.Attribute;
import de.dfki.step.kb.AttributeValue;
import de.dfki.step.kb.Entity;
import org.pcollections.PSequence;
import org.pcollections.PSet;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class ResolutionComponent implements Component {

    private static Logger log = LoggerFactory.getLogger(ResolutionComponent.class);
    private ComponentManager cm;
    private Supplier<Collection<Entity>> personSupplier;
    private Supplier<Collection<Entity>> physicalEntitySupplier;
    private Supplier<Collection<Entity>> sessionSupplier;
    private PSequence<ReferenceDistribution> distributions = TreePVector.empty();


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

    public PSequence<ReferenceDistribution> getReferenceDistribution() {
        return distributions;
    }

    @Override
    public void update() {
        PSet<Token> tokens = cm.retrieveComponent(InputComponent.class).getTokens();
        for(Token token: tokens) {
            distributions.plus(doResolution(token));
        }
    }


    public ReferenceDistribution doResolution(Token token) {

        ReferenceResolver rr = null;
        ReferenceDistribution distribution = new ReferenceDistribution();

        if(token.has("slots")) {
            List<Map<String,Object>> slots = (List<Map<String, Object>>) token.get("slots").get();
            for(Map<String, Object> slotinfo: slots) {
                if(slotinfo.containsKey("slot_type")) {
                    if(slotinfo.get("slot_type").equals("entity")) {
                        if(slotinfo.get("entity_type").equals("person")) {
                            rr = new PersonRR(personSupplier);
                        }else if(slotinfo.get("entity_type").equals("personal_pronoun")) {
                            rr = new PersonPronounRR(personSupplier, sessionSupplier);
                        }else {
                            rr = new ObjectRR(physicalEntitySupplier);
                            if(slotinfo.containsKey("personal_pronoun")) {
                                ((ObjectRR) rr).setPronoun((String)slotinfo.get("personal_pronoun"));
                            }
                            if(slotinfo.containsKey("attributes")) {
                                Map<Attribute, AttributeValue> attrMap = new HashMap<>();
                                List<Map<String, Object>> attributes = (List) slotinfo.get("attributes");
                                for(Map<String, Object> attr: attributes) {
                                    Attribute a = new Attribute((String)attr.get("attribute_type"));
                                    String aValue = (String) attr.get("attribute_value");
                                    attrMap.put(a, new AttributeValue(aValue, aValue, a));
                                }
                                ((ObjectRR) rr).setAttrMap(attrMap);

                            }
                        }
                    }else if(slotinfo.get("slot_type").equals("location")) {
                        //TODO
                    }
                }
            }


        }
        if(rr != null) {
            distribution = rr.getReferences();
        }

        return distribution;
    }

    @Override
    public Object createSnapshot() {
        return null;
    }

    @Override
    public void loadSnapshot(Object snapshot) {

    }

}
