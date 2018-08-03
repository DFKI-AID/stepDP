package de.dfki.tocalog.model;

public interface Service extends de.dfki.tractat.idl.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getUri();
    Service setUri(java.lang.String value);
    
    java.util.Optional<java.lang.String> getType();
    Service setType(java.lang.String value);
    
    java.util.Optional<java.util.List<java.lang.String>> getComponents();
    Service setComponents(java.util.List<java.lang.String> value);
    

    
    java.util.Optional<java.lang.String> getId();
    Service setId(java.lang.String value);
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Service setTimestamp(java.lang.Long value);
    
    java.util.Optional<java.lang.String> getSource();
    Service setSource(java.lang.String value);
    

    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    static Service create() {
        return new de.dfki.tocalog.model.ServiceImpl();
    }

    interface Factory {
        Service create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.ServiceImpl();
}

