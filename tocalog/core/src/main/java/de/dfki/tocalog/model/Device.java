package de.dfki.tocalog.model;

public interface Device extends de.dfki.tractat.idl.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getName();
    Device setName(java.lang.String value);
    

    
    java.util.Optional<java.lang.String> getId();
    Device setId(java.lang.String value);
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Device setTimestamp(java.lang.Long value);
    
    java.util.Optional<java.lang.String> getSource();
    Device setSource(java.lang.String value);
    

    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    static Device create() {
        return new de.dfki.tocalog.model.DeviceImpl();
    }

    interface Factory {
        Device create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.DeviceImpl();
}

