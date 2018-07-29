package de.dfki.tocalog.model;

public interface Person extends de.dfki.tractat.idl.Base {
    //getter / setter
    
    java.util.Optional<java.lang.String> getName();
    Person setName(java.lang.String value);
    
    java.util.Optional<java.util.List<java.lang.String>> getThings();
    Person setThings(java.util.List<java.lang.String> value);
    
    java.util.Optional<java.lang.String> getFriend();
    Person setFriend(java.lang.String value);
    
    java.util.Optional<java.util.Map<java.lang.String,java.lang.Double>> getMeta();
    Person setMeta(java.util.Map<java.lang.String,java.lang.Double> value);
    
    java.util.Optional<Person> getWuhu();
    Person setWuhu(Person value);
    
    java.util.Optional<java.util.List<Person>> getFoo();
    Person setFoo(java.util.List<Person> value);
    
    java.util.Optional<java.lang.String> getId();
    Person setId(java.lang.String value);
    
    java.util.Optional<byte[]> getBin();
    Person setBin(byte[] value);
    


    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    public static Person create() {
        return new PersonImpl();
    }

    interface Factory {
        Person create();
    }

    Factory factory = () -> new PersonImpl();
}

