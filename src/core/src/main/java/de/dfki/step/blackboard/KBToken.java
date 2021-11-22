package de.dfki.step.blackboard;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;

public class KBToken extends AbstractToken {
    @JsonProperty
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
    @JsonIdentityReference(alwaysAsId = true)
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
    }

	@Override
	public void setString(String propertyName, String value) {
		_parent.setString(propertyName, value);
	}

	@Override
	public void setInteger(String propertyName, Integer value) {
		_parent.setInteger(propertyName, value);
	}

	@Override
	public void setBoolean(String propertyName, Boolean value) {
		_parent.setBoolean(propertyName, value);
	}

	@Override
	public void setFloat(String propertyName, Float value) {
		_parent.setFloat(propertyName, value);
	}

	@Override
	public void setReference(String propertyName, UUID value) {
		_parent.setReference(propertyName, value);
	}

	@Override
	public void setStringArray(String propertyName, String[] value) {
		_parent.setStringArray(propertyName, value);
	}

	@Override
	public void setIntegerArray(String propertyName, Integer[] value) {
		_parent.setIntegerArray(propertyName, value);
	}

	@Override
	public void setBooleanArray(String propertyName, Boolean[] value) {
		_parent.setBooleanArray(propertyName, value);
	}

	@Override
	public void setFloatArray(String propertyName, Float[] value) {
		_parent.setFloatArray(propertyName, value);
	}

	@Override
	public void setReferenceArray(String propertyName, UUID[] value) {
		_parent.setReferenceArray(propertyName, value);
	}

	@Override
	public void setReference(String propertyName, Object value) {
        this._parent.setReference(propertyName, value);
	}

	@Override
	public void addReferenceToArray(String propertyName, UUID value) {
		this._parent.addReferenceToArray(propertyName, value);
	}

    @Override
    public Object internal_getContent() {
        return this._parent.getUUID().toString();
    }
    @Override
    public IToken internal_createCopyWithChanges(Map<String, Object> newValues) throws Exception {
        if (newValues.entrySet().isEmpty())
            return new KBToken(this.getKB(), this._parent);
        else
            throw new Exception("Cannot change values in a reference to a kb object.");
    }

    @Override
    public Object internal_getContent() {
        return this._parent.getUUID().toString();
    }

}
