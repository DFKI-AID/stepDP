package de.dfki.step.kb;

import de.dfki.step.kb.semantic.IProperty;

import java.util.UUID;

public interface IKBObject {

    boolean hasProperty(String propertyName);
    IProperty getProperty(String propertyName);
    boolean isSet(String propertyName);

    String getString(String propertyName);
    Integer getInteger(String propertyName);
    Boolean getBoolean(String propertyName);
    Float getFloat(String propertyName);
    UUID getReference(String propertyName);

}
