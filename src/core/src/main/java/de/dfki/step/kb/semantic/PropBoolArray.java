package de.dfki.step.kb.semantic;

import de.dfki.step.kb.KnowledgeBase;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class PropBoolArray implements IProperty{
    private String _name;
    private boolean _value[] = null;
    private boolean _isConstant = false;
    private boolean _valueSet = false;
    private boolean _mustBePresent = false;
    private UUID _uuid = UUID.randomUUID();
    private KnowledgeBase _parent;

    public PropBoolArray(String name, KnowledgeBase parent) throws Exception
    {
        if(name == null)
            throw new Exception("no valid name for a type");
        if(parent == null)
            throw new Exception("no valid Knowledge Base for reference");

        this._name = name;
        this._parent = parent;

        // Register at the global UUID Storage
        this._parent.addUUIDtoList(this);
    }

    public void setConstantValue(boolean val[])
    {
        this._valueSet = true;
        this._value = val;
    }

    public boolean[] getConstantValue()
    {
        return this._value;
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
    public boolean isConstant() {
        return this._isConstant;
    }

    @Override
    public void setConstant(boolean val) {
        this._isConstant = val;
    }

    @Override
    public void clearConstantValue() throws Exception {
        if(this.isConstant())
            throw new Exception("Property is Constant and cannot be changed!");

        this._valueSet = false;
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

        PropBoolArray propBool = (PropBoolArray) o;

        if (!_name.equals(propBool._name)) return false;
        if (!Objects.equals(_value, propBool._value)) return false;
        if (!Objects.equals(_valueSet, propBool._valueSet)) return false;
        return Objects.equals(_mustBePresent, propBool._mustBePresent);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(_name, _isConstant, _valueSet, _mustBePresent, _uuid, _parent);
        result = 31 * result + Arrays.hashCode(_value);
        return result;
    }

    public Object clone() throws CloneNotSupportedException
    {
        try {
            PropBoolArray copy = new PropBoolArray(this._name, this._parent);

            copy.setConstant(false);
            copy.setConstantValue(this.getConstantValue());
            copy.setConstant(this.isConstant());
            copy.setMustBePresent(this.mustBePresent());

            return copy;
        }
        catch(Exception e)
        {
            throw new CloneNotSupportedException("Cloning failed!");
        }
    }

    @Override
    public UUID getUUID() {
        return this._uuid;
    }
}
