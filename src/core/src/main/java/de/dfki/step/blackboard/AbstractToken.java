package de.dfki.step.blackboard;

import de.dfki.step.kb.KnowledgeBase;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.*;

public abstract class AbstractToken implements IToken {

    private long _timestamp;
    private final UUID _uuid = UUID.randomUUID();
    private boolean _active = true;
    private Integer _deleteTime = null;
    private Integer _ignoreTime = null;
    private LinkedList<String> _ignoreRuleTags = new LinkedList<>();
    private final List<UUID> _usedBy = new LinkedList<>();
    private final List<UUID> _checkedBy = new LinkedList<>();
    private KnowledgeBase _parentKB;
    // tokens that were created out of this token (e.g. by fusion) associated with the corresponding
    // rule UUID
    private MultiValuedMap<UUID, IToken> _resultingTokens = new HashSetValuedHashMap<UUID, IToken>();
    // tokens from which this token was created (e.g.by fusion)
    private List<IToken> _originTokens = new ArrayList<IToken>();
    // rule that created the token
    private UUID _producer; 

    public AbstractToken(KnowledgeBase kb)
    {
        this._parentKB = kb;
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
        this._resultingTokens.putAll(uuid, tokens);
    }

    @Override
    public MultiValuedMap<UUID, IToken> getResultingTokens() {
    	return this._resultingTokens;
    }

    @Override
    public KnowledgeBase getKB() {
    	return this._parentKB;
    }

}
