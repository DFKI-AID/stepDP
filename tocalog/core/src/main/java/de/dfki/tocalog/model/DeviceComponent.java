package de.dfki.tocalog.model;

public interface DeviceComponent extends de.dfki.tractat.idl.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getDevice();
    DeviceComponent setDevice(java.lang.String value);
    

    
    java.util.Optional<java.lang.String> getId();
    DeviceComponent setId(java.lang.String value);
    
    java.util.Optional<java.lang.Long> getTimestamp();
    DeviceComponent setTimestamp(java.lang.Long value);
    
    java.util.Optional<java.lang.String> getSource();
    DeviceComponent setSource(java.lang.String value);
    

    void deserialize(de.dfki.tractat.idl.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.tractat.idl.Serializer serializer) throws java.io.IOException;

    static DeviceComponent create() {
        return new de.dfki.tocalog.model.DeviceComponentImpl();
    }

    interface Factory {
        DeviceComponent create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.DeviceComponentImpl();
}

