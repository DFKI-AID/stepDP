package de.dfki.tocalog.model;

public interface Entity extends de.dfki.sire.Base {
    //getter / setter
    
    java.util.Optional<java.lang.String> getId();
    Entity setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Entity setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Entity setSource(java.lang.String value);
    boolean isSourcePresent();
    

    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    Entity copy();

    static Entity create() {
        return new de.dfki.tocalog.model.EntityImpl();
    }

    interface Factory {
        Entity create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.EntityImpl();
}

