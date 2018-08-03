package de.dfki.tocalog.model;

public interface Person extends de.dfki.tractat.idl.Base, de.dfki.tocalog.model.Agent {
    //getter / setter
    
    java.util.Optional<java.lang.Long> getAge();
    Person setAge(java.lang.Long value);
    
    java.util.Optional<java.lang.String> getGender();
    Person setGender(java.lang.String value);
    

    
    java.util.Optional<java.lang.String> getName();
    Person setName(java.lang.String value);
    
    java.util.Optional<java.lang.String> getId();
    Person setId(java.lang.String value);
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Person setTimestamp(java.lang.Long value);
    
    java.util.Optional<java.lang.String> getSource();
    Person setSource(java.lang.String value);
    

    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    static Person create() {
        return new de.dfki.tocalog.model.PersonImpl();
    }

    interface Factory {
        Person create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.PersonImpl();
}

