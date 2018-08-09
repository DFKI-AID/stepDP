package de.dfki.tocalog.model;

public interface Vector3 extends de.dfki.sire.Base {
    //getter / setter
    
    java.util.Optional<java.lang.Double> getX();
    Vector3 setX(java.lang.Double value);
    boolean isXPresent();
    
    java.util.Optional<java.lang.Double> getY();
    Vector3 setY(java.lang.Double value);
    boolean isYPresent();
    
    java.util.Optional<java.lang.Double> getZ();
    Vector3 setZ(java.lang.Double value);
    boolean isZPresent();
    

    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    static Vector3 create() {
        return new de.dfki.tocalog.model.Vector3Impl();
    }

    interface Factory {
        Vector3 create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.Vector3Impl();
}

