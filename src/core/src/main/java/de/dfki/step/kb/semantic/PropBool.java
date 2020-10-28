package de.dfki.step.kb.semantic;

import java.util.Objects;

public class PropBool implements IProperty{
    private String _name;
    private Boolean _value = null;
    private Boolean _valueSet = false;
    private Boolean _mustBePresent = false;

    public void setValue(Boolean val)
    {
        this._valueSet = true;
        this._value = val;
    }

    public Boolean getValue()
    {
        return this._value;
    }

    public void deleteValue()
    {
        this._valueSet = false;
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
        return _valueSet;
    }

    @Override
    public boolean canCompare(IProperty otherProp) {
        if(otherProp.getClass() == PropString.class)
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

        PropBool propBool = (PropBool) o;

        if (!_name.equals(propBool._name)) return false;
        if (!Objects.equals(_value, propBool._value)) return false;
        if (!Objects.equals(_valueSet, propBool._valueSet)) return false;
        return Objects.equals(_mustBePresent, propBool._mustBePresent);
    }

    @Override
    public int hashCode() {
        int result = _name.hashCode();
        result = 31 * result + (_value != null ? _value.hashCode() : 0);
        result = 31 * result + (_valueSet != null ? _valueSet.hashCode() : 0);
        result = 31 * result + (_mustBePresent != null ? _mustBePresent.hashCode() : 0);
        return result;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return null;
    }
}
