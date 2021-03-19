package de.dfki.step.kb.semantic;

import de.dfki.step.kb.KnowledgeBase;

import java.util.Objects;
import java.util.UUID;

public class PropReference implements IProperty {

    private String _name;
    private boolean _isConstant = false;
    private boolean _mustBePresent = false;
    private Type _type;
    private UUID _uuid = UUID.randomUUID();
    private UUID _reference = null;
    private KnowledgeBase _parent;

    public PropReference(String name, KnowledgeBase parent, Type type) throws Exception
    {
        if(name == null)
            throw new Exception("no valid name for a type");
        if(parent == null)
            throw new Exception("no valid Knowledge Base for reference");
        if(type == null)
        	throw new Exception("reference properties must specify a type for the referenced object");

        this._name = name;
        this._parent = parent;
        this._type = type;

        // Register at the global UUID Storage
        this._parent.addUUIDtoList(this);
    }


    public void setConstantReference(UUID ref)
    {
        this._reference = ref;
    }

    public UUID getConstantValue()
    {
        return this._reference;
    }

    public Type getType() {
    	return this._type;
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
        this._reference = null;
    }

    @Override
    public boolean hasValue() {
        return _reference != null;
    }

    @Override
    public boolean canCompare(IProperty otherProp) {
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
        PropReference that = (PropReference) o;
        return _isConstant == that._isConstant &&
                _mustBePresent == that._mustBePresent &&
                _name.equals(that._name) &&
                _type.equals(that._type) &&
                _uuid.equals(that._uuid) &&
                _reference.equals(that._reference) &&
                Objects.equals(_parent, that._parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _isConstant, _mustBePresent, _type, _uuid, _reference, _parent);
    }
}
