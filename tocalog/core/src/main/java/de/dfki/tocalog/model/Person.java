package de.dfki.tocalog.model;

public interface Person extends de.dfki.tractat.idl.Base, de.dfki.tocalog.model.Entity {
    //getter / setter

    java.util.Optional<String> getName();
    Person setName(String value);

    java.util.Optional<java.util.List<String>> getThings();
    Person setThings(java.util.List<String> value);



    java.util.Optional<String> getId();
    Person setId(String value);

    java.util.Optional<java.util.List<String>> getYo();
    Person setYo(java.util.List<String> value);


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

