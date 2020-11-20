package de.dfki.step.blackboard;

import de.dfki.step.kb.semantic.Type;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Token {
    private static final Logger log = LoggerFactory.getLogger(Token.class);

    private long _timestamp;
    private final UUID _uuid = UUID.randomUUID();
    private Type _type;
    private boolean _active;
    private Integer _deleteTime = null;
    private Integer _ignoreTime = null;
    private String _ignoreRuleTags[];
    private final List<UUID> _usedBy = new LinkedList<>();
    private PMap<String, Object> _payload = HashTreePMap.empty();

    public Token()
    {
        this._timestamp = new Date().getTime();
    }

    /**
     * get the timestamp of the creation time of the token in unixtime (milliseconds)
     * @return
     */
    public long getTimestamp() {
        return _timestamp;
    }

    private void setTimestamp(long timestamp) {
        this._timestamp = timestamp;
    }

    public UUID getUUID() {
        return _uuid;
    }

    public Type getType() {
        return _type;
    }

    public void setType(Type type) {
        this._type = type;
    }

    public boolean isActive() {
        return _active;
    }

    /**
     * Set the active state of the token. If a token is not active, it will not be matched with any rule
     * @param active new value of the token state
     */
    public void setActive(boolean active) {
        this._active = active;
    }

    public Integer getDeleteTime() {
        return _deleteTime;
    }

    /**
     * Overrides the default value of the delete time from the blackboard
     * @param deleteTime new delete time or null for default
     */
    public void setDeleteTime(Integer deleteTime) {
        this._deleteTime = deleteTime;
    }

    public Integer getIgnoreTime() {
        return _ignoreTime;
    }

    /**
     * Overrides the default value of the ignore time from the blackboard
     * @param ignoreTime new ignore time or null for default
     */
    public void setIgnoreTime(Integer ignoreTime) {
        this._ignoreTime = ignoreTime;
    }

    /**
     * Rules containing one of the tags will not be matched
     * @return
     */
    public String[] getIgnoreRuleTags() {
        return _ignoreRuleTags;
    }

    public void setIgnoreRuleTags(String[] _ignoreRuleTags) {
        this._ignoreRuleTags = _ignoreRuleTags;
    }

    /**
     * UUID of the rules that already consumed this token
     * @return
     */
    public List<UUID> getUsedBy()
    {
        return _usedBy;
    }

    public boolean isUsedBy(UUID uuid)
    {
        return this._usedBy.contains(uuid);
    }

    public void usedBy(UUID uuid)
    {
        this._usedBy.add(uuid);
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
            if(obj instanceof Token) {
                obj = ((Token) obj)._payload;
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
            this._payload = this._payload.plus(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Object> getPayload()
    {
        return this._payload;
    }

}
