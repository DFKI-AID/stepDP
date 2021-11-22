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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KBObject implements IKBObjectWriteable
{
    private String _name;
    private Type _type;
    private KnowledgeBase _parent;
    private final UUID _uuid = UUID.randomUUID();
    private Map<String, Object> _data = new HashMap<String, Object>();
    private TokenObject _rootTokenObject;
    private ObjectMapper mapper = new ObjectMapper();

    protected KBObject(String name, Type type, KnowledgeBase parent)
    {
        this._name = name;
        this._type = type;
        this._parent = parent;
        this._rootTokenObject = new TokenObject(this, this._data, this._parent);
    }

    protected KBObject(String name, Type type, KnowledgeBase parent, Map<String, Object> data) throws Exception
    {
        this(name, type, parent);
        if (data != null) {
            Map<String, Object> deepCopy =  mapper.readValue(mapper.writeValueAsString(data), new TypeReference<Map<String, Object>>() {});
            this._data.putAll(deepCopy);
        }
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
        return _rootTokenObject.getString(propertyName);
    }

    @Override
    public Integer getInteger(String propertyName) {
        return _rootTokenObject.getInteger(propertyName);
    }

    @Override
    public Boolean getBoolean(String propertyName) {
        return _rootTokenObject.getBoolean(propertyName);
    }

    @Override
    public Float getFloat(String propertyName) {
        return _rootTokenObject.getFloat(propertyName);
    }

    @Override
    public UUID getReference(String propertyName) {
        return _rootTokenObject.getReference(propertyName);
    }

    @Override
    public IKBObjectWriteable getResolvedReference(String propertyName) {
        return _rootTokenObject.getResolvedReference(propertyName);
    }

    @Override
    public String[] getStringArray(String propertyName) {
        return _rootTokenObject.getStringArray(propertyName);
    }

    @Override
    public Integer[] getIntegerArray(String propertyName) {
        return _rootTokenObject.getIntegerArray(propertyName);
    }

    @Override
    public Boolean[] getBooleanArray(String propertyName) {
        return _rootTokenObject.getBooleanArray(propertyName);
    }

    @Override
    public Float[] getFloatArray(String propertyName) {
        return _rootTokenObject.getFloatArray(propertyName);
    }

    @Override
    public UUID[] getReferenceArray(String propertyName) {
        return _rootTokenObject.getReferenceArray(propertyName);
    }

    @Override
    public IKBObjectWriteable[] getResolvedReferenceArray(String propertyName) {
        return _rootTokenObject.getResolvedReferenceArray(propertyName);
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
