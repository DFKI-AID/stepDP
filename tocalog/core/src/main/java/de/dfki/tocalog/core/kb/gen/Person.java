package de.dfki.tocalog.core.kb.gen;

public interface Person extends de.dfki.tractat.idl.Base<Person> {
    //getter / setter
    
    java.util.Optional<String> getName();
    Person setName(String value);

    java.util.Optional<java.util.List<String>> getThings();
    Person setThings(java.util.List<String> value);

    java.util.Optional<String> getFriend();
    Person setFriend(String value);

    java.util.Optional<java.util.Map<String,Double>> getMeta();
    Person setMeta(java.util.Map<String, Double> value);

    java.util.Optional<Person> getWuhu();
    Person setWuhu(de.dfki.tocalog.core.kb.gen.Person value);

    java.util.Optional<java.util.List<Person>> getFoo();
    Person setFoo(java.util.List<Person> value);

    java.util.Optional<String> getId();
    Person setId(String value);



    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    public static Person create() {
        return new de.dfki.tocalog.core.kb.gen.PersonImpl();
    }

    interface Factory {
        Person create();
    }

    Factory factory = () -> new PersonImpl();
}

