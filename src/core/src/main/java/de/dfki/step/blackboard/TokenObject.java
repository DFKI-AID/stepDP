package de.dfki.step.blackboard;

import java.util.Map;
import java.util.UUID;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;
import org.pcollections.PMap;

public class TokenObject implements IKBObject {

	private Token _parent;
	private Map<String, Object> _payload;

    public TokenObject(Token _parent, Map<String, Object> _payload)
	{
		this._parent = _parent;
		this._payload = _payload;
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
		return this._parent.getType().hasProperty(propertyName);
	}

	@Override
	public IProperty getProperty(String propertyName) {
		return this._parent.getType().getProperty(propertyName);
	}

	@Override
	public boolean isSet(String propertyName) {
		return (this._payload.get(propertyName) != null); 
	}

	@Override
	public Type getType() {
		// TODO: should return type of the nested token object not the parent
		return this._parent.getType();
	}

	@Override
	public String getString(String propertyName) {
		return this._payload.get(propertyName).toString();
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
		return null;
	}

	@Override
	public IKBObject getResolvedReference(String propertyName) {
		if (this.isSet(propertyName))
		{
			// TODO resolve UUIDs and names (KB reference)
			// write warning to log if name ambiguous; return first match
			return new TokenObject(this._parent, (Map<String, Object>) this._payload.get(propertyName));
		}
		else
			return null;
	}

}
