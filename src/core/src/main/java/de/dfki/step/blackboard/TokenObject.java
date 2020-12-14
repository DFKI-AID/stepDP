package de.dfki.step.blackboard;

import java.util.UUID;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;

public class TokenObject implements IKBObject {

/*
    public TokenObject(Token _parent, String SubDaten)

        _parent.getPayload()......
*/

	@Override
	public UUID getUUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasProperty(String propertyName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IProperty getProperty(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSet(String propertyName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getString(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getInteger(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getBoolean(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float getFloat(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getReference(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IKBObject getResolvedReference(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

}
