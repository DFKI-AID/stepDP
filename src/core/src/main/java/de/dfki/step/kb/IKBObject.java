package de.dfki.step.kb;

import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;

import java.util.UUID;

public interface IKBObject extends IUUID {

    String getName();

    boolean hasProperty(String propertyName);
    IProperty getProperty(String propertyName);
    boolean isSet(String propertyName);

    Type getType();
    String getString(String propertyName);
    Integer getInteger(String propertyName);
    Boolean getBoolean(String propertyName);
    Float getFloat(String propertyName);
    UUID getReference(String propertyName);
    IKBObjectWriteable getResolvedReference(String propertyName);

    String[] getStringArray(String propertyName);
    Integer[] getIntegerArray(String propertyName);
    Boolean[] getBooleanArray(String propertyName);
    Float[] getFloatArray(String propertyName);
    UUID[] getReferenceArray(String propertyName);
    IKBObjectWriteable[] getResolvedReferenceArray(String propertyName);

}
