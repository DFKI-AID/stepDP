package de.dfki.tocalog.model;

public interface VisualFocus extends de.dfki.tractat.idl.Base {
    //getter / setter
    
    java.util.Optional<String> getId();
    VisualFocus setId(String value);

    java.util.Optional<String> getFocus();
    VisualFocus setFocus(String value);
    


    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    public static VisualFocus create() {
        return new VisualFocusImpl();
    }

    interface Factory {
        VisualFocus create();
    }

    Factory factory = () -> new VisualFocusImpl();
}

