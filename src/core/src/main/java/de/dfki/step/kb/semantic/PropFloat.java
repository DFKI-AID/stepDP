package de.dfki.step.kb.semantic;

import java.util.Objects;
import java.util.UUID;

public class PropFloat implements IProperty {
    private String _name;
    private Float _value = null;
    private Boolean _mustBePresent = false;
    private UUID _uuid = UUID.randomUUID();

    public void setValue(Float val)
    {
        this._value = val;
    }

    public Float getValue()
    {
        return this._value;
    }

    public void deleteValue()
    {
        this._value = null;
    }

    @Override
    public String getName() {
        return this._name;
    }

    @Override
    public void setName(String name) {
        this._name  = name;
    }

    @Override
    public boolean mustBePresent() {
        return this._mustBePresent;
    }

    @Override
    public void setMustBePresent(boolean val) {
        this._mustBePresent = val;
    }

    @Override
    public boolean hasValue() {
        return _value != null;
    }

    @Override
    public boolean canCompare(IProperty otherProp) {
        if(otherProp.getClass() == PropInt.class)
            return true;

        if(otherProp.getClass() == PropFloat.class)
            return true;

        return false;
    }

    @Override
    public String serialize() {
        return null;
    }

    @Override
    public void deserialize(String data) {

    }

    @Override
    public UUID getUUID() {
        return this._uuid;
    }

    @Override
    public int compare(IProperty o1, IProperty o2) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropFloat propFloat = (PropFloat) o;

        if (!Objects.equals(_name, propFloat._name)) return false;
        if (!Objects.equals(_value, propFloat._value)) return false;
        return _mustBePresent.equals(propFloat._mustBePresent);
    }

    @Override
    public int hashCode() {
        int result = _name != null ? _name.hashCode() : 0;
        result = 31 * result + (_value != null ? _value.hashCode() : 0);
        result = 31 * result + _mustBePresent.hashCode();
        return result;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return null;
    }
}
