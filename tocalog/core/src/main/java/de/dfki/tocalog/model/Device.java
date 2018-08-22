package de.dfki.tocalog.model;

public interface Device extends de.dfki.sire.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getName();
    Device setName(java.lang.String value);
    boolean isNamePresent();
    

    
    java.util.Optional<java.lang.String> getId();
    Device setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Device setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Device setSource(java.lang.String value);
    boolean isSourcePresent();
    
    java.util.Optional<java.lang.Double> getConfidence();
    Device setConfidence(java.lang.Double value);
    boolean isConfidencePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    Device copy();

    static Device create() {
        return new de.dfki.tocalog.model.DeviceImpl();
    }

    interface Factory {
        Device create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.DeviceImpl();
}

