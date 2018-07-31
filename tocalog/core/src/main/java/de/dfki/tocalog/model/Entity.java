package de.dfki.tocalog.model;

public interface Entity extends de.dfki.tractat.idl.Base {
    //getter / setter
    
    java.util.Optional<String> getId();
    Entity setId(String value);

    java.util.Optional<java.util.List<String>> getYo();
    Entity setYo(java.util.List<String> value);




    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    static Entity create() {
        return new de.dfki.tocalog.model.EntityImpl();
    }

    interface Factory {
        Entity create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.EntityImpl();
}

