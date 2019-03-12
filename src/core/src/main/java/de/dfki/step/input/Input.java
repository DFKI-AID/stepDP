package de.dfki.step.input;

public interface Input {
    String getId();
    long getTimestamp();
    String getInitiator();
    void setInitiator(String initiator);
}
