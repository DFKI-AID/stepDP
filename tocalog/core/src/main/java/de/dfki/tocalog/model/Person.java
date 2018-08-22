package de.dfki.tocalog.model;

public interface Person extends de.dfki.sire.Base, de.dfki.tocalog.model.Agent {
    //getter / setter
    
    java.util.Optional<java.lang.Long> getAge();
    Person setAge(java.lang.Long value);
    boolean isAgePresent();
    
    java.util.Optional<java.lang.String> getGender();
    Person setGender(java.lang.String value);
    boolean isGenderPresent();
    

    
    java.util.Optional<java.lang.String> getName();
    Person setName(java.lang.String value);
    boolean isNamePresent();
    
    java.util.Optional<java.lang.String> getId();
    Person setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Person setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Person setSource(java.lang.String value);
    boolean isSourcePresent();
    
    java.util.Optional<java.lang.Double> getConfidence();
    Person setConfidence(java.lang.Double value);
    boolean isConfidencePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    Person copy();

    static Person create() {
        return new de.dfki.tocalog.model.PersonImpl();
    }

    interface Factory {
        Person create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.PersonImpl();
}

