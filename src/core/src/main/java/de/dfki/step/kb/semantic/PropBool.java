package de.dfki.step.kb.semantic;

import de.dfki.step.kb.KnowledgeBase;

import java.util.Objects;
import java.util.UUID;

public class PropBool implements IProperty{
    private String _name;
    private boolean _value = false;
    private boolean _isConstant = false;
    private boolean _valueSet = false;
    private boolean _mustBePresent = false;
    private UUID _uuid = UUID.randomUUID();
    private KnowledgeBase _parent;

    public PropBool(String name, KnowledgeBase parent) throws Exception
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
    public boolean isConstant() {
        return this._isConstant;
    }

    @Override
    public void setConstant(boolean val) {
        this._isConstant = val;
    }

    @Override
    public void clearValue() throws Exception {
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

        PropBool propBool = (PropBool) o;

        if (!_name.equals(propBool._name)) return false;
        if (!Objects.equals(_value, propBool._value)) return false;
        if (!Objects.equals(_valueSet, propBool._valueSet)) return false;
        return Objects.equals(_mustBePresent, propBool._mustBePresent);
    }

    @Override
    public int hashCode() {
        int result = _name.hashCode();
        result = 31 * result + (_value ? 1 : 0);
        result = 31 * result + (_isConstant ? 1 : 0);
        result = 31 * result + (_valueSet ? 1 : 0);
        result = 31 * result + (_mustBePresent ? 1 : 0);
        result = 31 * result + _uuid.hashCode();
        return result;
    }

    public Object clone() throws CloneNotSupportedException
    {
        try {
            PropBool copy = new PropBool(this._name, this._parent);

            copy.setConstant(this.isConstant());
            copy.setMustBePresent(this.mustBePresent());
            copy.setValue(this.getValue());

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
