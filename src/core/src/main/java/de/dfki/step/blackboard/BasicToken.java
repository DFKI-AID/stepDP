package de.dfki.step.blackboard;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class BasicToken extends AbstractToken {
    private static final Logger log = LoggerFactory.getLogger(BasicToken.class);

    private Type _type;
    private Map<String, Object> _payload = new HashMap<String, Object>();
    private TokenObject _rootTokenObject;

    public BasicToken(KnowledgeBase kb)
    {
        super(kb);
        this._rootTokenObject = new TokenObject(this, this._payload, this.getKB());
        
    }

    @Override
    public Type getType() {
        return _type;
    }

    public void setType(Type type) {
        this._type = type;
        this.addAll(Map.of("type", type.getName()));
    }

    public Optional<Object> get(String key)
    {
        return Optional.ofNullable(this._payload.get(key));
    }

    public Optional<Object> get(String... keys)
    {
        return get(List.of(keys));
    }

    public Optional<Object> get(List<String> keys)
    {
        if (keys.isEmpty()) {
            return Optional.empty();
        }

        Optional<Object> firstObj = get(keys.get(0));
        if (!firstObj.isPresent()) {
            return Optional.empty();
        }

        Object obj = firstObj.get();

        for (int i = 1; i < keys.size(); i++) {
            if(obj instanceof BasicToken) {
                obj = ((BasicToken) obj).getPayload();
            }
            if (!(obj instanceof Map)) {
                return Optional.empty();
            }

            obj = ((Map<String, Object>) obj).get(keys.get(i));
            if (obj == null) {
                return Optional.empty();
            }
        }
        return Optional.of(obj);
    }

    public <T> Optional<T> get(Class<T> clazz, List<String> keys)
    {
        Optional<Object> obj = get(keys);
        if (!obj.isPresent()) {
            return Optional.empty();
        }
        if (!clazz.isAssignableFrom(obj.get().getClass())) {
            return Optional.empty();
        }
        return Optional.of((T) obj.get());
    }

    public <T> Optional<T> get(Class<T> clazz, String... keys)
    {
        return get(clazz, List.of(keys));
    }

    public <T> Optional<T> get(String key, Class<T> clazz)
    {
        if (!has(key)) {
            return Optional.empty();
        }
        Object obj = this._payload.get(key);
        if (!clazz.isAssignableFrom(obj.getClass())) {
            return Optional.empty();
        }

        return Optional.of((T) obj);
    }

    public boolean has(String key)
    {
        return this._payload.get(key) != null;
    }

    public <T> boolean has(String key, Class<T> clazz)
    {
        if (this._payload.get(key) == null) {
            return false;
        }
        return clazz.isAssignableFrom(this._payload.get(key).getClass());
    }

    public void addAll(Map<String, Object> values) {
        for (var entry : values.entrySet()) {
            this._payload.put(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Object> getPayload()
    {
        return this._payload;
    }

	@Override
	public String getName() {
		return "TokenObject";
	}

	@Override
	public boolean hasProperty(String propertyName) {
		return _rootTokenObject.hasProperty(propertyName);
	}

	@Override
	public IProperty getProperty(String propertyName) {
		return _rootTokenObject.getProperty(propertyName);
	}

	@Override
	public boolean isSet(String propertyName) {
		return _rootTokenObject.isSet(propertyName);
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
	public IKBObject getResolvedReference(String propertyName) {
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
    public IKBObject[] getResolvedReferenceArray(String propertyName) {
        return _rootTokenObject.getResolvedReferenceArray(propertyName);
    }

    @Override
    public IToken createTokenWithSameContent() {
    	ObjectMapper mapper = new ObjectMapper();
    	Map<String, Object> deepCopy;
    	try {
			deepCopy = mapper.readValue(mapper.writeValueAsString(_payload), new TypeReference<Map<String, Object>>() {});
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    	BasicToken newToken = new BasicToken(this.getKB());
    	newToken.setType(this.getType());
    	newToken.addAll(deepCopy);
    	return newToken;
    };

}
