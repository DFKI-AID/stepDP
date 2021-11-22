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

    // serialize only type name, not whole Type object
    @JsonProperty
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
    @JsonIdentityReference(alwaysAsId = true)
    public Type getType();

    @JsonProperty("active")
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

    public void setOriginTokens(List<IToken> originTokens);

    /**
     * Returns the tokens that served as input for creating this token (e.g. during fusion).
     */
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
    public MultiValuedMap<UUID, IToken> getResultingTokens();

    public KnowledgeBase getKB();

    public  IToken createTokenWithSameContent();

    /**
     * Only for internal use (no public API).
     * Helper method to insert the content of a token into a new token, e.g. during fusion.
     * @throws Exception if there was a problem while copying the content
     */
    public Object internal_getContent() throws Exception;

    /**
     * Only for internal use (no public API).
     * @param newValues a map from property names to their new values (can be a nested map for complex tokens)
     * @return a new token with the same content as this except for the values provided in newValues
     * @throws exception if a value should be changed in a kb object reference or if a problem occured while
     * copying the token's content
     */
    public IToken internal_createCopyWithChanges(Map<String, Object> newValues) throws Exception;

    /**
     * Only for internal use (no public API).
     * Helper method to insert the content of a token into a new token, e.g. during fusion.
     * @throws Exception if there was a problem while copying the content
     */
    public Object internal_getContent() throws Exception;
}
