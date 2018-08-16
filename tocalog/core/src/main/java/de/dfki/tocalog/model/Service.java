package de.dfki.tocalog.model;

public interface Service extends de.dfki.sire.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getUri();
    Service setUri(java.lang.String value);
    boolean isUriPresent();
    
    java.util.Optional<java.lang.String> getType();
    Service setType(java.lang.String value);
    boolean isTypePresent();
    
    java.util.Optional<java.util.List<java.lang.String>> getComponents();
    Service setComponents(java.util.List<java.lang.String> value);
    boolean isComponentsPresent();
    

    
    java.util.Optional<java.lang.String> getId();
    Service setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Service setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Service setSource(java.lang.String value);
    boolean isSourcePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    Service copy();

    static Service create() {
        return new de.dfki.tocalog.model.ServiceImpl();
    }

    interface Factory {
        Service create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.ServiceImpl();
}

