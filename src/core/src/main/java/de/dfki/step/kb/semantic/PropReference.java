package de.dfki.step.kb.semantic;

import java.util.UUID;

public class PropReference implements IProperty {

    private String _name;
    private Boolean _mustBePresent = false;
    private UUID _uuid = UUID.randomUUID();
    private UUID _reference = null;

    public void setReference(UUID ref)
    {
        this._reference = ref;
    }

    public UUID getReference()
    {
        return this._reference;
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
}
