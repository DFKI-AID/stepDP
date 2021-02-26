package de.dfki.step.blackboard;

import java.util.UUID;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;

public class KBToken extends AbstractToken {
    private IKBObject _parent;

    public KBToken(KnowledgeBase kb, IKBObject kbObj) {
        super(kb);
        this._parent = kbObj;
    }

    @Override
    public Type getType() {
        return _parent.getType();
    }

    @Override
    public String getName() {
        return _parent.getName();
    }

    @Override
    public boolean hasProperty(String propertyName) {
        return _parent.hasProperty(propertyName);
    }

    @Override
    public IProperty getProperty(String propertyName) {
        return _parent.getProperty(propertyName);
    }

    @Override
    public boolean isSet(String propertyName) {
        return _parent.isSet(propertyName);
    }

    @Override
    public String getString(String propertyName) {
        return _parent.getString(propertyName);
    }

    @Override
    public Integer getInteger(String propertyName) {
        return _parent.getInteger(propertyName);
    }

    @Override
    public Boolean getBoolean(String propertyName) {
        return _parent.getBoolean(propertyName);
    }

    @Override
    public Float getFloat(String propertyName) {
        return _parent.getFloat(propertyName);
    }

    @Override
    public UUID getReference(String propertyName) {
        return _parent.getReference(propertyName);
    }

    @Override
    public IKBObject getResolvedReference(String propertyName) {
        return _parent.getResolvedReference(propertyName);
    }

}
