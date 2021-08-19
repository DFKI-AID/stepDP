package de.dfki.step.kb;

import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    protected KBObject(String name, Type type, KnowledgeBase parent, Map<String, Object> data)
    {
        this(name, type, parent);
        this._data = data;
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
        Object data = this._data.get(propertyName);
        if (data instanceof UUID)
            return (UUID) data;
        else
            return null;
    }

    @Override
    public IKBObjectWriteable getResolvedReference(String propertyName) {
        Object data = this._data.get(propertyName);
        if (data instanceof UUID)
            return this._parent.getInstanceWriteable((UUID) data);
        // FIXME: find prettier solution for this
        else if(data instanceof Map)
        {
            Map<String, Object> inner = (Map<String, Object>) this._data.get(propertyName);
            Type typeOfObject = null;
            if(inner.containsKey("type"))
            {
                String type = inner.get("type").toString();
                typeOfObject = this._parent.getType(type);
            }
            return new KBObject(null, typeOfObject,  this._parent, (Map<String, Object>) data);
        }
        else
            return null;
    }

    @Override
    public String[] getStringArray(String propertyName) {
        return (String[])this._data.get(propertyName);
    }

    @Override
    public Integer[] getIntegerArray(String propertyName) {
        return (Integer[])this._data.get(propertyName);
    }

    @Override
    public Boolean[] getBooleanArray(String propertyName) {
        return (Boolean[])this._data.get(propertyName);
    }

    @Override
    public Float[] getFloatArray(String propertyName) {
        return (Float[])this._data.get(propertyName);
    }

    @Override
    public UUID[] getReferenceArray(String propertyName) {
        return (UUID[])this._data.get(propertyName);
    }

    @Override
    public IKBObjectWriteable[] getResolvedReferenceArray(String propertyName) {
        UUID[] uuids = getReferenceArray(propertyName);
        IKBObjectWriteable[] result = new IKBObjectWriteable[uuids.length];

        for(int i = 0; i < uuids.length; i++)
        {
            result[i] = this._parent.getInstanceWriteable(uuids[i]);
        }
        return result;
    }

    @Override
    public void setString(String propertyName, String value) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setInteger(String propertyName, Integer value) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setBoolean(String propertyName, Boolean value) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setFloat(String propertyName, Float value) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setReference(String propertyName, UUID value) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setStringArray(String propertyName, String[] value) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setIntegerArray(String propertyName, Integer[] value) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setBooleanArray(String propertyName, Boolean[] value) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setFloatArray(String propertyName, Float[] value) {
        this._data.put(propertyName, value);
    }

    @Override
    public void setReferenceArray(String propertyName, UUID[] value) {
        this._data.put(propertyName, value);
    }

    @Override
    public UUID getUUID() {
        return this._uuid;
    }

	@Override
	public void setReference(String propertyName, Object value) {
        this._data.put(propertyName, value);
	}

	@Override
	public void addReferenceToArray(String propertyName, UUID value) {
       UUID[] uuids = getReferenceArray(propertyName);
       List<UUID> uuidList = Arrays.asList(uuids);
       uuidList.add(value);
       this._data.put(propertyName, uuidList.toArray());
	}
}
