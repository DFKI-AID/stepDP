package de.dfki.step.kb;

import de.dfki.step.blackboard.IToken;
import de.dfki.step.blackboard.TokenObject;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReferenceArray;
import de.dfki.step.kb.semantic.Type;

import java.util.ArrayList;
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
    	Object data = this._data.get(propertyName);
		if(data instanceof Integer)
		{
			return (float)((int) data);
		}
		else if(data instanceof Double)
		{
			return (float)((double) data);
		}
		else if(data instanceof Float)
		{
			return (float) data;
		}
		else return null;
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
		if (!this.isSet(propertyName))
			return null;

		ArrayList<Object> tmp = (ArrayList<Object>) this._data.get(propertyName);
		Float[] result = new Float[tmp.size()];
		for(int i = 0; i < tmp.size(); i++)
		{
			Object listEntry = tmp.get(i);

			if(listEntry instanceof Integer)
			{
				result[i] = (float)((int)tmp.get(i));
			}
			else if(listEntry instanceof Double)
			{
				result[i] = (float)((double)tmp.get(i));
			}
			else if(listEntry instanceof Float)
			{
				result[i] = (float)(tmp.get(i));
			}
		}
		return result;
    }

    @Override
    public UUID[] getReferenceArray(String propertyName) {
		if (!this.isSet(propertyName))
			return null;

		try
		{
			ArrayList<Object> data = (ArrayList<Object>)this._data.get(propertyName);
			UUID[] results = new UUID[data.size()];

			for(int i = 0; i < data.size(); i++)
			{
				Object var = data.get(i);

				if(var instanceof String)
				{
					results[i] = UUID.fromString((String)var);
				}
			}

			return results;
		} catch (IllegalArgumentException exception){
			return null;
		}
    }

    @Override
    public IKBObjectWriteable[] getResolvedReferenceArray(String propertyName) {
		if (!this.isSet(propertyName))
			return null;

		IProperty prop = this.getProperty(propertyName);
		Type typeOfObject = null;
		if(prop != null && prop instanceof PropReferenceArray)
		{
			typeOfObject = ((PropReferenceArray)prop).getType();
		}
		else
		{
			typeOfObject = this._parent.getRootType();
		}

		try
		{
			Object refArray = this._data.get(propertyName);
			List<Object> data;
			if (refArray instanceof Object[])
				data = Arrays.asList(((Object[]) this._data.get(propertyName)));
			else
				data = (List<Object>) this._data.get(propertyName);
			IKBObjectWriteable[] results = new IKBObjectWriteable[data.size()];

			for(int i = 0; i < data.size(); i++)
			{
				Object var = data.get(i);

				if(var instanceof String)
				{
					UUID uuid = null;
					try{
						uuid = UUID.fromString(var.toString());
					} catch (IllegalArgumentException exception){
					}

					IKBObjectWriteable ref;
					if(uuid != null) {
						ref = this._parent.getInstanceWriteable(uuid);
					}
					else
						ref = this._parent.getInstanceWriteable(var.toString());

					if(ref != null)
						results[i] = ref;
				}
				else
				{
					// something bad happend?
				}
			}

			return results;
		} catch (IllegalArgumentException exception){
			return null;
		}
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
