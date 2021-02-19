package de.dfki.step.blackboard;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.MultiValuedMap;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.Type;

public interface IToken extends IKBObject{
    /**
     * get the timestamp of the creation time of the token in unixtime (milliseconds)
     * @return
     */
    public long getTimestamp();

    public UUID getUUID();

    public Type getType();

    public boolean isActive();

    /**
     * Set the active state of the token. If a token is not active, it will not be matched with any rule
     * @param active new value of the token state
     */
    public void setActive(boolean active);

    public Integer getDeleteTime();

    /**
     * Overrides the default value of the delete time from the blackboard
     * @param deleteTime new delete time or null for default
     */
    public void setDeleteTime(Integer deleteTime);

    public Integer getIgnoreTime();

    /**
     * Overrides the default value of the ignore time from the blackboard
     * @param ignoreTime new ignore time or null for default
     */
    public void setIgnoreTime(Integer ignoreTime);

    /**
     * Rules containing one of the tags will not be matched
     * @return
     */
    public LinkedList<String> getIgnoreRuleTags();

    public void setIgnoreRuleTags(LinkedList<String> _ignoreRuleTags);

    public boolean isIgnoredBy(List<String> tags);

    /**
     * UUID of the rules that already consumed this token
     * @return
     */
    public List<UUID> getUsedBy();

    public boolean isUsedBy(UUID uuid);

    public void usedBy(UUID uuid);

    public boolean isCheckedBy(UUID uuid);

    public void checkedBy(UUID uuid);

    public Map<String, Object> getPayload();

    public void setOriginTokens(List<IToken> originTokens);

    /**
     * Returns the tokens that served as input for creating this token (e.g. during fusion).
     */
    // to avoid infinite recursion during serialization, only origin tokens are
    // serialized (@JsonManagedReference) and resulting tokens not (@JsonBackReference)
    @JsonManagedReference
    // TODO: find a better solution for this problem, e.g. serialization by UUID?
    public List<IToken> getOriginTokens();

    public void setProducer(UUID producer);

    /**
     * Returns the UUID of the rule that created this token or null if it was not created by a rule.
     */
    public UUID getProducer();

    public void addResultingTokens(List<IToken> tokens, UUID uuid);

    /**
     * Returns the tokens that were created from this token (e.g. through fusion).
     */
    // to avoid infinite recursion during serialization, only origin tokens are
    // serialized (@JsonManagedReference) and resulting tokens not (@JsonBackReference)
    // TODO: find a better solution for this problem, e.g. serialization by UUID?
    @JsonBackReference
    public MultiValuedMap<UUID, IToken> getResultingTokens();

    public KnowledgeBase getKB();
}
