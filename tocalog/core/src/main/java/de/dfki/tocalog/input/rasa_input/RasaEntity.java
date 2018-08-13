package de.dfki.tocalog.input.rasa_input;

public class RasaEntity {

    private String value;

    private String entity;

    private float confidence;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "RasaEntity{" +
                "value='" + value + '\'' +
                ", entity='" + entity + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
