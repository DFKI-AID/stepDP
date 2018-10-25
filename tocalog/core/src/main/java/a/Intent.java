package a;

import de.dfki.tocalog.model.Confidence;

import java.util.*;

/**
 */
@Deprecated
public class Intent {
    private CommunicativeFunction communicativeFunction;
    private String type;
    private Entity nominative = new Entity(); //wer oder was
    private String genetive; //wessen
    private String dative; //wem
    private Entity accusative = new Entity(); //wen
    private Optional<Confidence> confidence = Optional.empty();
    private Entity location = new Entity();


    public static class Entity {
        private List<String> entities = new ArrayList<>();

        public void addEntity(String entity) {
            entities.add(entity);
        }

        public List<String> getEntities() {
            return entities;
        }
    }


    public enum CommunicativeFunction {
        Question,
        Statement,
        Request
    }

    public Intent(CommunicativeFunction communicativeFunction, String intent) {
        this.communicativeFunction = communicativeFunction;
        this.type = intent;
    }

    public Entity getNominative() {
        return nominative;
    }

    public void addNominative(String nominative) {
        this.nominative.addEntity(nominative);
    }

    public String getGenetive() {
        return genetive;
    }

    public void setGenetive(String genetive) {
        this.genetive = genetive;
    }

    public String getDative() {
        return dative;
    }

    public void setDative(String dative) {
        this.dative = dative;
    }

    public Entity getAccusative() {
        return accusative;
    }

    public void addAccusative(String entity) {
        this.accusative.addEntity(entity);
    }

    public CommunicativeFunction getCommunicativeFunction() {
        return communicativeFunction;
    }

    public String getType() {
        return type;
    }

    public Optional<Confidence> getConfidence() {
        return confidence;
    }

    public void setConfidence(Optional<Confidence> confidence) {
        this.confidence = confidence;
    }
}
