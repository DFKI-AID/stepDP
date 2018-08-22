package de.dfki.tocalog.model;

public interface Zone extends de.dfki.sire.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getZone();
    Zone setZone(java.lang.String value);
    boolean isZonePresent();
    

    
    java.util.Optional<java.lang.String> getId();
    Zone setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Zone setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Zone setSource(java.lang.String value);
    boolean isSourcePresent();
    
    java.util.Optional<java.lang.Double> getConfidence();
    Zone setConfidence(java.lang.Double value);
    boolean isConfidencePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    Zone copy();

    static Zone create() {
        return new de.dfki.tocalog.model.ZoneImpl();
    }

    interface Factory {
        Zone create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.ZoneImpl();
}

