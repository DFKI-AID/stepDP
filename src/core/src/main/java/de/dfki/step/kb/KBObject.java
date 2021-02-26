package de.dfki.step.kb;

import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KBObject implements IKBObjectWriteable
{
    private String _name;
    private Type _type;
    private KnowledgeBase _parent;
    private final UUID _uuid = UUID.randomUUID();
    private Map<String, Object> _data = new HashMap<String, Object>();

    protected KBObject(String name, Type type, KnowledgeBase parent)
    {
    	this._name = name;
        this._type = type;
        this._parent = parent;
    }

    @Override
    public String getName() {
        return this._name;
    }

    @Override
    public boolean hasProperty(String propertyName) {
        return this._type.hasProperty(propertyName);
    }

    @Override
    public IProperty getProperty(String propertyName) {
        return this._type.getProperty(propertyName);
    }

    @Override
    public boolean isSet(String propertyName) {
    	return (this._data.get(propertyName) != null); 
    }

    @Override
    public Type getType() {
        return this._type;
    }

    @Override
    public String getString(String propertyName) {
        Object value = this._data.get(propertyName);
        if (value == null)
            return null;
        return value.toString();
    }

    @Override
    public Integer getInteger(String propertyName) {
        return (Integer) this._data.get(propertyName);
    }

    @Override
    public Boolean getBoolean(String propertyName) {
        return (Boolean) this._data.get(propertyName);
    }

    @Override
    public Float getFloat(String propertyName) {
        return (Float) this._data.get(propertyName);
    }

    @Override
    public UUID getReference(String propertyName) {
        return (UUID) this._data.get(propertyName);
    }

    @Override
    public IKBObject getResolvedReference(String propertyName) {
        return this._parent.getInstance(this.getReference(propertyName));
    }

    @Override
    public void setString(String value, String propertyName) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setInteger(Integer value, String propertyName) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setBoolean(Boolean value, String propertyName) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setFloat(Float value, String propertyName) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setReference(UUID value, String propertyName) {
        this._data.put(propertyName, value);
    }

    @Override
    public UUID getUUID() {
        return this._uuid;
    }
}
