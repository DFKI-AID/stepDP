package de.dfki.step.rasa;

public class RasaEntity {

    private String value;

    private String entity;

    private int start;

    private int end;

    private double confidence;

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

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
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
