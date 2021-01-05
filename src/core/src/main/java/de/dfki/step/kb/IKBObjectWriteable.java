package de.dfki.step.kb;

import java.util.UUID;

public interface IKBObjectWriteable extends IKBObject {

    void setString(String value, String propertyName);
    void setInteger(Integer value, String propertyName);
    void setBoolean(Boolean value, String propertyName);
    void setFloat(Float value, String propertyName);
    void setReference(UUID value, String propertyName);
}
