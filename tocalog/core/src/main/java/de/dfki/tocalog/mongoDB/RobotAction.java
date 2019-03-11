package de.dfki.tocalog.mongoDB;

import de.dfki.tocalog.core.Hypothesis;
import de.dfki.tocalog.core.Slot;
import de.dfki.tocalog.kb.Ontology;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;
import java.util.Map;

@Entity("RobotAction")
public class RobotAction {
    @Id
    private String id;
    private String name;
    private Map<String, String> customNames;
    private Hypothesis actionHypo;

    public RobotAction(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Hypothesis getActionHypothesis() {
        return actionHypo;
    }

    public void setActionHypothesis(List<Slot> slots) {
        Hypothesis.Builder actionHypoBuilder = new Hypothesis.Builder(name);
        for(Slot slot: slots) {
            actionHypoBuilder.addSlot(slot);
        }
        actionHypo = actionHypoBuilder.build();
    }


    public Map<String, String> getCustomNames() {
        return customNames;
    }

    public void setCustomNames(Map<String, String> customNames) {
        this.customNames = customNames;
    }

    public void addCustomName(String personId, String name) {
        customNames.put(personId, name);
    }
}
