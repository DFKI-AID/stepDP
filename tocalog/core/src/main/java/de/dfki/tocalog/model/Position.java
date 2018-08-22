package de.dfki.tocalog.model;

public interface Position extends de.dfki.sire.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<Vector3> getPosition();
    Position setPosition(Vector3 value);
    boolean isPositionPresent();
    

    
    java.util.Optional<java.lang.String> getId();
    Position setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Position setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Position setSource(java.lang.String value);
    boolean isSourcePresent();
    
    java.util.Optional<java.lang.Double> getConfidence();
    Position setConfidence(java.lang.Double value);
    boolean isConfidencePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    Position copy();

    static Position create() {
        return new de.dfki.tocalog.model.PositionImpl();
    }

    interface Factory {
        Position create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.PositionImpl();
}

