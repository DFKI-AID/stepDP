package de.dfki.tocalog.model;

public interface Agent extends de.dfki.tractat.idl.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getName();
    Agent setName(java.lang.String value);
    

    
    java.util.Optional<java.lang.String> getId();
    Agent setId(java.lang.String value);
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Agent setTimestamp(java.lang.Long value);
    
    java.util.Optional<java.lang.String> getSource();
    Agent setSource(java.lang.String value);
    

    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    static Agent create() {
        return new de.dfki.tocalog.model.AgentImpl();
    }

    interface Factory {
        Agent create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.AgentImpl();
}

