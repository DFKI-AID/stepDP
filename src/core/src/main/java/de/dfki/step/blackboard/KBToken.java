package de.dfki.step.blackboard;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;

public class KBToken extends AbstractToken {
    private IKBObject _parent;

    public KBToken(KnowledgeBase kb, IKBObject kbObj) {
        super(kb);
        if (kbObj instanceof IToken)
            throw new RuntimeException("KBToken can only hold references to kb objects, not other tokens.");
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

    @Override
    public String[] getStringArray(String propertyName) {
        return _parent.getStringArray(propertyName);
    }

    @Override
    public Integer[] getIntegerArray(String propertyName) {
        return _parent.getIntegerArray(propertyName);
    }

    @Override
    public Boolean[] getBooleanArray(String propertyName) {
        return _parent.getBooleanArray(propertyName);
    }

    @Override
    public Float[] getFloatArray(String propertyName) {
        return _parent.getFloatArray(propertyName);
    }

    @Override
    public UUID[] getReferenceArray(String propertyName) {
        return _parent.getReferenceArray(propertyName);
    }

    @Override
    public IKBObject[] getResolvedReferenceArray(String propertyName) {
        return _parent.getResolvedReferenceArray(propertyName);
    }

    @Override
    public IToken createTokenWithSameContent() {
    	return new KBToken(this.getKB(), _parent);
    };
}
