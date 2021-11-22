package de.dfki.step.blackboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IKBObjectWriteable;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropReferenceArray;
import de.dfki.step.kb.semantic.Type;
import org.pcollections.PMap;

public class TokenObject implements IKBObject {

	private IKBObject _parent;
	private Map<String, Object> _payload;
	private KnowledgeBase _kb;
	private Type _type;

    public TokenObject(IKBObject parent, Map<String, Object> payload, KnowledgeBase kb)
	{
		this._parent = parent;
		this._payload = payload;
		this._kb = kb;
		this._type = null;
	}

	public TokenObject(IKBObject parent, Map<String, Object> payload, KnowledgeBase kb, Type type)
	{
		this._parent = parent;
		this._payload = payload;
		this._kb = kb;
		this._type = type;
	}

	@Override
	public UUID getUUID() {
		return this._parent.getUUID();
	}

	@Override
	public String getName() {
		return this._parent.getName();
	}

	@Override
	public boolean hasProperty(String propertyName) {
		return this.getType().hasProperty(propertyName);
	}

	@Override
	public IProperty getProperty(String propertyName) {
		return this.getType().getProperty(propertyName);
	}

	@Override
	public boolean isSet(String propertyName) {
		return (this._payload.get(propertyName) != null); 
	}

	@Override
	public Type getType() {
		// TODO: should return type of the nested token object not the parent

		// check if type is provided by the JSON payload
		if(this._payload.containsKey("type"))
		{
			String type = this._payload.get("type").toString();
			Type typeObj = this._kb.getType(type);

			return typeObj;
		}

		// if not, check if type is provided on creating time of the Token Object
		if(this._type != null)
			return this._type;

		// if not, give back the type of the parent
		return this._parent.getType();
	}

	@Override
	public String getString(String propertyName) {
		Object value = this._payload.get(propertyName);
		if (value == null)
			return null;
		return value.toString();
	}

	@Override
	public Integer getInteger(String propertyName) {
		return (Integer) this._payload.get(propertyName);
	}

	@Override
	public Boolean getBoolean(String propertyName) {
		return (Boolean) this._payload.get(propertyName);
	}

	@Override
	public Float getFloat(String propertyName) {
    	Object data = this._payload.get(propertyName);
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
	    if (this.isSet(propertyName)) {
	        Object data = this._payload.get(propertyName);
	        if(data instanceof String)
            {
                try{
                    return UUID.fromString(data.toString());
                } catch (IllegalArgumentException exception){
                    return null;
                }
            } else if (data instanceof UUID) {
                return (UUID) data;
            }
	    }
		return null;
	}

	@Override
	public IKBObject getResolvedReference(String propertyName) {
        if (!this.isSet(propertyName))
            return null;
    // TODO resolve UUIDs and names (KB reference)
    // TODO write warning to log if name ambiguous; return first match (must be implemented in KnowledgeBase)

    IProperty prop = this.getProperty(propertyName);
    Object data = this._payload.get(propertyName);
    return resolveReference(prop, data);
	}

	@Override
	public String[] getStringArray(String propertyName) {
		if (!this.isSet(propertyName))
			return null;

        List<String> data = getList(propertyName, new ArrayList<String>());
        if (data != null)
            return data.toArray(new String[data.size()]);
        else
            return null;
	}

	@Override
	public Integer[] getIntegerArray(String propertyName) {
		if (!this.isSet(propertyName))
			return null;

        List<Integer> data = getList(propertyName, new ArrayList<Integer>());
        if (data != null)
            return data.toArray(new Integer[data.size()]);
        else
            return null;
	}

	@Override
	public Boolean[] getBooleanArray(String propertyName) {
		if (!this.isSet(propertyName))
			return null;

        List<Boolean> data = getList(propertyName, new ArrayList<Boolean>());
        if (data != null)
            return data.toArray(new Boolean[data.size()]);
        else
            return null;
	}

	@Override
	public Float[] getFloatArray(String propertyName) {
		if (!this.isSet(propertyName))
			return null;

        Float[] results = null;
        List<Object> data = getList(propertyName, new ArrayList<Object>());
        if (data != null) {
            results = new Float[data.size()];
        } else {
            // something bad happened?
            return null;
        }

		for(int i = 0; i < data.size(); i++)
		{
			Object listEntry = data.get(i);

			if(listEntry instanceof Integer)
			{
				results[i] = (float)((int)data.get(i));
			}
			else if(listEntry instanceof Double)
			{
				results[i] = (float)((double)data.get(i));
			}
			else if(listEntry instanceof Float)
			{
				results[i] = (float)(data.get(i));
			}
		}
		return results;
	}

	@Override
	public UUID[] getReferenceArray(String propertyName) {
		if (!this.isSet(propertyName))
			return null;

		try
		{
            UUID[] results = null;
            List<Object> data = getList(propertyName, new ArrayList<Object>());
            if (data != null) {
                results = new UUID[data.size()];
            } else {
                // something bad happened?
                return null;
            }

			for(int i = 0; i < data.size(); i++)
			{
				Object var = data.get(i);

				if(var instanceof String)
				{
					results[i] = UUID.fromString((String)var);
				}
				else if (var instanceof UUID) 
				{
				    results[i] = (UUID) var;
				}
			}

			return results;
		} catch (IllegalArgumentException exception){
			return null;
		}
	}

	@Override
	public IKBObject[] getResolvedReferenceArray(String propertyName) {
		if (!this.isSet(propertyName))
			return null;

		IProperty prop = this.getProperty(propertyName);

		try
		{
            IKBObject[] results = null;
            List<Object> data = getList(propertyName, new ArrayList<Object>());
            if (data != null) {
                results = new IKBObject[data.size()];
            } else {
                Object value = this._payload.get(propertyName);
                IKBObject result = resolveReference(prop, value);
                if (result != null)
                    return new IKBObject[] {result};
            }

			for(int i = 0; i < data.size(); i++)
			{
                results[i] = resolveReference(prop, data.get(i));
			}

			return results;
		} catch (IllegalArgumentException exception){
			return null;
		}
	}

    private IKBObject resolveReference(IProperty prop, Object var) {
        Type typeOfObject = null;
        if(prop != null && prop instanceof PropReference)
        {
            typeOfObject = ((PropReference)prop).getType();
        }
        else if(prop != null && prop instanceof PropReferenceArray)
        {
            typeOfObject = ((PropReferenceArray)prop).getType();
        }
        else
        {
            typeOfObject = this._kb.getRootType();
        }

        if(var instanceof UUID || var instanceof String)
        {
            UUID uuid = null;
            if (var instanceof UUID)
                uuid = (UUID) var;
            else {
            try{
                uuid = UUID.fromString(var.toString());
            } catch (IllegalArgumentException exception){
            }
            }

            IKBObject ref;
            if(uuid != null)
                ref = this._kb.getInstance(uuid);
            else
                ref = this._kb.getInstance(var.toString());

            return ref;
        }
        else if(var instanceof Map)
        {
            return new TokenObject(this._parent, (Map<String, Object>) var, this._kb, typeOfObject);
        }
        else if(var instanceof IToken)
        {
            return (IToken) var;
        }
        else
        {
            // something bad happend?
            return null;
        }
    }

    private <T> List<T> getList(String propertyName, List<T> typedList){
        Object value = this._payload.get(propertyName);
        List<T> data = null;
        if (value instanceof List) {
            data = (List<T>)this._payload.get(propertyName);
        } else if (value instanceof Object[]) {
            data = List.of((T[])this._payload.get(propertyName));
        }
        return data;
    }

	public Map<String, Object> getPayload() {
		return this._payload;
	}

}
