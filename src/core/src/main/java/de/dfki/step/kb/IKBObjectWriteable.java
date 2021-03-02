package de.dfki.step.kb;

import java.util.UUID;

public interface IKBObjectWriteable extends IKBObject {

    void setString(String propertyName, String value);
    void setInteger(String propertyName, Integer value);
    void setBoolean(String propertyName, Boolean value);
    void setFloat(String propertyName, Float value);
    void setReference(String propertyName, UUID value);
}
