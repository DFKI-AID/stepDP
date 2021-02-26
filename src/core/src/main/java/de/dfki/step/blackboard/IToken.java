package de.dfki.step.blackboard;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.MultiValuedMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.Type;

// don't detect any fields for (de)serialization automatically but
// (de)serialize only the fields whose getter is annotated with @JsonProperty
@JsonAutoDetect(
        fieldVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        creatorVisibility = Visibility.NONE
    )
public interface IToken extends IKBObject{
    /**
     * get the timestamp of the creation time of the token in unixtime (milliseconds)
     * @return
     */
    @JsonProperty
    public long getTimestamp();

    @JsonProperty
    public UUID getUUID();

    @JsonProperty
    public Type getType();

    @JsonProperty("active")
    public boolean isActive();

    /**
     * Set the active state of the token. If a token is not active, it will not be matched with any rule
     * @param active new value of the token state
     */
    public void setActive(boolean active);

    @JsonProperty
    public Integer getDeleteTime();

    /**
     * Overrides the default value of the delete time from the blackboard
     * @param deleteTime new delete time or null for default
     */
    public void setDeleteTime(Integer deleteTime);

    @JsonProperty
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
    @JsonProperty
    public LinkedList<String> getIgnoreRuleTags();

    public void setIgnoreRuleTags(LinkedList<String> _ignoreRuleTags);

    public boolean isIgnoredBy(List<String> tags);

    /**
     * UUID of the rules that already consumed this token
     * @return
     */
    @JsonProperty
    public List<UUID> getUsedBy();

    public boolean isUsedBy(UUID uuid);

    public void usedBy(UUID uuid);

    public boolean isCheckedBy(UUID uuid);

    public void checkedBy(UUID uuid);

    public void setOriginTokens(List<IToken> originTokens);

    /**
     * Returns the tokens that served as input for creating this token (e.g. during fusion).
     */
    // serialize as id to avoid infinite recursion
    @JsonProperty
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uuid")
    @JsonIdentityReference(alwaysAsId = true) 
    public List<IToken> getOriginTokens();

    public void setProducer(UUID producer);

    /**
     * Returns the UUID of the rule that created this token or null if it was not created by a rule.
     */
    @JsonProperty
    public UUID getProducer();

    public void addResultingTokens(List<IToken> tokens, UUID uuid);

    /**
     * Returns the tokens that were created from this token (e.g. through fusion).
     */
    public MultiValuedMap<UUID, IToken> getResultingTokens();

    public KnowledgeBase getKB();
}
