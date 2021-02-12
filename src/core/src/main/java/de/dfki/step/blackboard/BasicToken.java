package de.dfki.step.blackboard;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;
import de.dfki.step.util.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.*;

public class BasicToken implements IToken {
    private static final Logger log = LoggerFactory.getLogger(BasicToken.class);

    private long _timestamp;
    private final UUID _uuid = UUID.randomUUID();
    private Type _type;
    private boolean _active = true;
    private Integer _deleteTime = null;
    private Integer _ignoreTime = null;
    private LinkedList<String> _ignoreRuleTags = new LinkedList<>();
    private final List<UUID> _usedBy = new LinkedList<>();
    private final List<UUID> _checkedBy = new LinkedList<>();
    private KnowledgeBase _parentKB;
    private Map<String, Object> _payload = new HashMap<String, Object>();
    private TokenObject _rootTokenObject;
    // tokens that were created out of this token (e.g. by fusion)
    private List<Tuple<List<IToken>, UUID>> _resultingTokens = new ArrayList<Tuple<List<IToken>, UUID>>();
    // tokens from which this token was created (e.g.by fusion)
    private List<IToken> _originTokens = new ArrayList<IToken>();
    // rule that created the token
    private UUID _producer; 

    public BasicToken(KnowledgeBase kb)
    {
        this._parentKB = kb;
        this._rootTokenObject = new TokenObject(this, this._payload, this._parentKB);
        this._timestamp = new Date().getTime();
    }

    @Override
    public long getTimestamp() {
        return _timestamp;
    }

    @Override
    public UUID getUUID() {
        return _uuid;
    }

    @Override
    public Type getType() {
        return _type;
    }

    @Override
    public void setType(Type type) {
        this._type = type;
        this.addAll(Map.of("type", type.getName()));
    }

    @Override
    public boolean isActive() {
        return _active;
    }

    @Override
    public void setActive(boolean active) {
        this._active = active;
    }

    @Override
    public Integer getDeleteTime() {
        return _deleteTime;
    }

    @Override
    public void setDeleteTime(Integer deleteTime) {
        this._deleteTime = deleteTime;
    }

    @Override
    public Integer getIgnoreTime() {
        return _ignoreTime;
    }

    @Override
    public void setIgnoreTime(Integer ignoreTime) {
        this._ignoreTime = ignoreTime;
    }

    @Override
    public LinkedList<String> getIgnoreRuleTags() {
        return _ignoreRuleTags;
    }

    @Override
    public void setIgnoreRuleTags(LinkedList<String> _ignoreRuleTags) {
        this._ignoreRuleTags = _ignoreRuleTags;
    }

    @Override
    public boolean isIgnoredBy(List<String> tags)
    {
        if(tags == null)
            return false;

        for(String s : tags)
        {
            if(this._ignoreRuleTags.contains(s))
                return true;
        }
        return false;
    }

    @Override
    public List<UUID> getUsedBy()
    {
        return _usedBy;
    }

    @Override
    public boolean isUsedBy(UUID uuid)
    {
        return this._usedBy.contains(uuid);
    }

    @Override
    public void usedBy(UUID uuid)
    {
        this._usedBy.add(uuid);
    }

    @Override
    public boolean isCheckedBy(UUID uuid)
    {
        return this._checkedBy.contains(uuid);
    }

    @Override
    public void checkedBy(UUID uuid)
    {
        this._checkedBy.add(uuid);
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

    @Override
    public Map<String, Object> getPayload()
    {
        return this._payload;
    }

    @Override
    public void setOriginTokens(List<IToken> originTokens) {
    	this._originTokens = originTokens;
    }

    @Override
    public List<IToken> getOriginTokens() {
    	return this._originTokens;
    }

    @Override
    public void setProducer(UUID producer) {
    	this._producer = producer;
    }

    @Override
    public UUID getProducer() {
    	return this._producer;
    }

    @Override
    public void addResultingTokens(List<IToken> tokens, UUID uuid) {
    	this._resultingTokens.add(new Tuple<List<IToken>, UUID>(tokens, uuid));
    }

    @Override
    public List<Tuple<List<IToken>, UUID>> getResultingTokens() {
    	return this._resultingTokens;
    }

    @Override
    public KnowledgeBase getKB() {
    	return this._parentKB;
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

}
