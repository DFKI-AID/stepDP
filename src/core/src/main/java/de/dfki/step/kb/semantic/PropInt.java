package de.dfki.step.kb.semantic;

import java.util.Objects;

public class PropInt implements IProperty{
    private String _name;
    private Integer _value = null;
    private Boolean _mustBePresent = false;

    public void setValue(Integer val)
    {
        this._value = val;
    }

    public Integer getValue()
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
        return _value == null;
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
    public int compare(IProperty o1, IProperty o2) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropInt propInt = (PropInt) o;

        if (!Objects.equals(_name, propInt._name)) return false;
        if (!Objects.equals(_value, propInt._value)) return false;
        return _mustBePresent.equals(propInt._mustBePresent);
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
