package de.dfki.tocalog.model;

public interface Robot extends de.dfki.sire.Base, de.dfki.tocalog.model.Agent {
    //getter / setter
    

    
    java.util.Optional<java.lang.String> getName();
    Robot setName(java.lang.String value);
    boolean isNamePresent();
    
    java.util.Optional<java.lang.String> getId();
    Robot setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Robot setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Robot setSource(java.lang.String value);
    boolean isSourcePresent();
    
    java.util.Optional<java.lang.Double> getConfidence();
    Robot setConfidence(java.lang.Double value);
    boolean isConfidencePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    Robot copy();

    static Robot create() {
        return new de.dfki.tocalog.model.RobotImpl();
    }

    interface Factory {
        Robot create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.RobotImpl();
}

