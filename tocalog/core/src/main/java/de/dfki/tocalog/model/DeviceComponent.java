package de.dfki.tocalog.model;

public interface DeviceComponent extends de.dfki.sire.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getDevice();
    DeviceComponent setDevice(java.lang.String value);
    boolean isDevicePresent();
    

    
    java.util.Optional<java.lang.String> getId();
    DeviceComponent setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    DeviceComponent setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    DeviceComponent setSource(java.lang.String value);
    boolean isSourcePresent();
    
    java.util.Optional<java.lang.Double> getConfidence();
    DeviceComponent setConfidence(java.lang.Double value);
    boolean isConfidencePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    DeviceComponent copy();

    static DeviceComponent create() {
        return new de.dfki.tocalog.model.DeviceComponentImpl();
    }

    interface Factory {
        DeviceComponent create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.DeviceComponentImpl();
}
