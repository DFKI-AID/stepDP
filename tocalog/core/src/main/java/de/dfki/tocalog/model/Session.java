package de.dfki.tocalog.model;

public interface Session extends de.dfki.sire.Base, de.dfki.tocalog.model.Entity {
    //getter / setter
    
    java.util.Optional<java.util.List<java.lang.String>> getAgents();
    Session setAgents(java.util.List<java.lang.String> value);
    boolean isAgentsPresent();
    

    
    java.util.Optional<java.lang.String> getId();
    Session setId(java.lang.String value);
    boolean isIdPresent();
    
    java.util.Optional<java.lang.Long> getTimestamp();
    Session setTimestamp(java.lang.Long value);
    boolean isTimestampPresent();
    
    java.util.Optional<java.lang.String> getSource();
    Session setSource(java.lang.String value);
    boolean isSourcePresent();
    

    void deserialize(de.dfki.sire.Deserializer deserializer) throws java.io.IOException;

    void serialize(de.dfki.sire.Serializer serializer) throws java.io.IOException;

    static Session create() {
        return new de.dfki.tocalog.model.SessionImpl();
    }

    interface Factory {
        Session create();
    }

    Factory factory = () -> new de.dfki.tocalog.model.SessionImpl();
}

