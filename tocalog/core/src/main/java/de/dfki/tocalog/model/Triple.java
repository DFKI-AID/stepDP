package de.dfki.tocalog.model;

public interface Triple extends de.dfki.sire.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.lang.String> getSubject();
    Triple setSubject(java.lang.String value);
    boolean isSubjectPresent();
    
    java.util.Optional<java.lang.String> getPredicate();
    Triple setPredicate(java.lang.String value);
    boolean isPredicatePresent();
    
    java.util.Optional<java.lang.String> getObject();
    Triple setObject(java.lang.String value);
    boolean isObjectPresent();
    

    
    java.util.Optional<java.lang.String> getId();
    Triple setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Triple setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Triple setSource(java.lang.String value);
    boolean isSourcePresent();
    
    java.util.Optional<java.lang.Double> getConfidence();
    Triple setConfidence(java.lang.Double value);
    boolean isConfidencePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    Triple copy();

    static Triple create() {
        return new de.dfki.tocalog.model.TripleImpl();
    }

    interface Factory {
        Triple create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.TripleImpl();
}

