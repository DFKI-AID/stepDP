package de.dfki.tocalog.model;

public interface Focus extends de.dfki.tractat.idl.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getAgent();
    Focus setAgent(java.lang.String value);
    
    java.util.Optional<java.lang.String> getFocus();
    Focus setFocus(java.lang.String value);
    

    
    java.util.Optional<java.lang.String> getId();
    Focus setId(java.lang.String value);
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Focus setTimestamp(java.lang.Long value);
    
    java.util.Optional<java.lang.String> getSource();
    Focus setSource(java.lang.String value);
    

    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    static Focus create() {
        return new de.dfki.tocalog.model.FocusImpl();
    }

    interface Factory {
        Focus create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.FocusImpl();
}

