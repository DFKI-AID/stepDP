package de.dfki.step.blackboard;

import java.util.Map;
import java.util.UUID;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.Type;
import org.pcollections.PMap;

public class TokenObject implements IKBObject {

	private BasicToken _parent;
	private Map<String, Object> _payload;
	private KnowledgeBase _kb;
	private Type _type;

    public TokenObject(BasicToken parent, Map<String, Object> payload, KnowledgeBase kb)
	{
		this._parent = parent;
		this._payload = payload;
		this._kb = kb;
		this._type = null;
	}

	public TokenObject(BasicToken parent, Map<String, Object> payload, KnowledgeBase kb, Type type)
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
		return (Float) this._payload.get(propertyName);
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
            }
	    }
		return null;
	}

	@Override
	public IKBObject getResolvedReference(String propertyName) {
		if (this.isSet(propertyName))
		{
			// TODO resolve UUIDs and names (KB reference)
			// TODO write warning to log if name ambiguous; return first match (must be implemented in KnowledgeBase)

			IProperty prop = this.getProperty(propertyName);
			Type typeOfObject = null;
			if(prop != null && prop instanceof PropReference)
			{
				typeOfObject = ((PropReference)prop).getType();
			}
			else
			{
				typeOfObject = this._kb.getRootType();
			}

			Object data = this._payload.get(propertyName);

			if(data instanceof String)
			{
				UUID uuid = null;
				try{
					uuid = UUID.fromString(data.toString());
				} catch (IllegalArgumentException exception){
				}

				IKBObject ref;
				if(uuid != null)
					ref = this._kb.getInstance(uuid);
				else
					ref = this._kb.getInstance(data.toString());
				if(ref != null)
					return ref;
				else
					return null;
			}
			else if(data instanceof Map)
			{
				return new TokenObject(this._parent, (Map<String, Object>) this._payload.get(propertyName), this._kb, typeOfObject);
			}
			else if(data instanceof IToken)
			{
			    return (IToken) data;
			}
			else
			{
				// something bad happend?
				return null;
			}
		}
		else
			return null;
	}

}
