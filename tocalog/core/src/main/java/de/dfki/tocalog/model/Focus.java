package de.dfki.tocalog.model;

public interface Focus extends de.dfki.sire.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getAgent();
    Focus setAgent(java.lang.String value);
    boolean isAgentPresent();
    
    java.util.Optional<java.lang.String> getFocus();
    Focus setFocus(java.lang.String value);
    boolean isFocusPresent();
    

    
    java.util.Optional<java.lang.String> getId();
    Focus setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Focus setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Focus setSource(java.lang.String value);
    boolean isSourcePresent();
    
    java.util.Optional<java.lang.Double> getConfidence();
    Focus setConfidence(java.lang.Double value);
    boolean isConfidencePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    Focus copy();

    static Focus create() {
        return new de.dfki.tocalog.model.FocusImpl();
    }

    interface Factory {
        Focus create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.FocusImpl();
}
