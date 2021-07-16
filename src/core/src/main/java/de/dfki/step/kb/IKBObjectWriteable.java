package de.dfki.step.kb;

import java.util.UUID;

public interface IKBObjectWriteable extends IKBObject {

    void setString(String propertyName, String value);
    void setInteger(String propertyName, Integer value);
    void setBoolean(String propertyName, Boolean value);
    void setFloat(String propertyName, Float value);
    void setReference(String propertyName, UUID value);
    void setReference(String propertyName, Object value);

    void setStringArray(String propertyName, String[] value);
    void setIntegerArray(String propertyName, Integer[] value);
    void setBooleanArray(String propertyName, Boolean[] value);
    void setFloatArray(String propertyName, Float[] value);
    void setReferenceArray(String propertyName, UUID[] value);
}
