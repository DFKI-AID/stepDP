package de.dfki.tocalog.model;

public interface Agent extends de.dfki.sire.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getName();
    Agent setName(java.lang.String value);
    boolean isNamePresent();
    

    
    java.util.Optional<java.lang.String> getId();
    Agent setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Agent setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Agent setSource(java.lang.String value);
    boolean isSourcePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    Agent copy();

    static Agent create() {
        return new de.dfki.tocalog.model.AgentImpl();
    }

    interface Factory {
        Agent create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.AgentImpl();
}

