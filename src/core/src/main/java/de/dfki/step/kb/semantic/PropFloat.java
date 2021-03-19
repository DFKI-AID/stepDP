package de.dfki.step.kb.semantic;

import de.dfki.step.kb.KnowledgeBase;

import java.util.Objects;
import java.util.UUID;

public class PropFloat implements IProperty {
    private String _name;
    private boolean _isConstant = false;
    private Float _value = null;
    private boolean _mustBePresent = false;
    private UUID _uuid = UUID.randomUUID();
    private KnowledgeBase _parent;

    public PropFloat(String name, KnowledgeBase parent) throws Exception
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

    public void setConstantValue(Float val)throws Exception {
        if(this.isConstant())
            throw new Exception("Property is Constant and cannot be changed!");
        this._value = val;
    }

    @Override
    public void setConstantValue(Object var) throws Exception {
        if(var instanceof  Float)
            this.setConstantValue((float) var);
        else
            throw new Exception("incompatible Object");
    }

    public Float getConstantValue()
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
        this._value = null;
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
        return _mustBePresent == propFloat._mustBePresent;
    }

    @Override
    public int hashCode() {
        int result = _name != null ? _name.hashCode() : 0;
        result = 31 * result + (_value != null ? _value.hashCode() : 0);
        result = 31 * result + (_mustBePresent ? 1 : 0);
        return result;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return null;
    }
}
