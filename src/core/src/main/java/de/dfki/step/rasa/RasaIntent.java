package de.dfki.step.rasa;

public class RasaIntent {

    private String name;

    private double confidence;

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
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
