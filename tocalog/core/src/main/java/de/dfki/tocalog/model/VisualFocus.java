package de.dfki.tocalog.model;

public interface VisualFocus extends de.dfki.tractat.idl.Base, de.dfki.tocalog.model.Entity {
    //getter / setter

    java.util.Optional<String> getFocus();
    VisualFocus setFocus(String value);

    java.util.Optional<String> getPerson();
    VisualFocus setPerson(String value);



    java.util.Optional<String> getId();
    VisualFocus setId(String value);

    java.util.Optional<java.util.List<String>> getYo();
    VisualFocus setYo(java.util.List<String> value);


    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    static VisualFocus create() {
        return new de.dfki.tocalog.model.VisualFocusImpl();
    }

    interface Factory {
        VisualFocus create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.VisualFocusImpl();
}

