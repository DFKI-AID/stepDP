package de.dfki.tocalog.rasa;

public class RasaIntent {

    private String name;

    private float confidence;

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "RasaIntent{" +
                "name='" + name + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
