package de.dfki.step.blackboard;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;
import de.dfki.step.util.Tuple;

public class KBToken implements IToken {

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

	@Override
	public long getTimestamp() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public UUID getUUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setType(Type type) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setActive(boolean active) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getDeleteTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDeleteTime(Integer deleteTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getIgnoreTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIgnoreTime(Integer ignoreTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public LinkedList<String> getIgnoreRuleTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIgnoreRuleTags(LinkedList<String> _ignoreRuleTags) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isIgnoredBy(List<String> tags) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<UUID> getUsedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUsedBy(UUID uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void usedBy(UUID uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCheckedBy(UUID uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void checkedBy(UUID uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<Object> get(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Object> get(String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Object> get(List<String> keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Optional<T> get(Class<T> clazz, List<String> keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Optional<T> get(Class<T> clazz, String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Optional<T> get(String key, Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean has(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> boolean has(String key, Class<T> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addAll(Map<String, Object> values) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> getPayload() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOriginTokens(List<BasicToken> originTokens) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<BasicToken> getOriginTokens() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProducer(UUID producer) {
		// TODO Auto-generated method stub

	}

	@Override
	public UUID getProducer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addResultingTokens(List<BasicToken> tokens, UUID uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Tuple<List<BasicToken>, UUID>> getResultingTokens() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KnowledgeBase getKB() {
		// TODO Auto-generated method stub
		return null;
	}

}
