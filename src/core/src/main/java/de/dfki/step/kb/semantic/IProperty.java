package de.dfki.step.kb.semantic;

import de.dfki.step.kb.IUUID;

import java.util.Comparator;

public interface IProperty extends Comparator<IProperty>, Cloneable, IUUID
{
    String getName();
    void setName(String name);

    boolean mustBePresent();
    void setMustBePresent(boolean val);

    boolean isConstant();
    void setConstant(boolean val);

    void clearConstantValue() throws Exception;
    Object getConstantValue();
    void setConstantValue(Object var) throws Exception;

    boolean hasValue();
    boolean canCompare(IProperty otherProp);

    String serialize();
    void deserialize(String data);
}
