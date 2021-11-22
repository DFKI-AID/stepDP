package de.dfki.step.blackboard.rules;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.Board;
import de.dfki.step.blackboard.Condition;
import de.dfki.step.blackboard.IToken;
import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.conditions.DeclarativeTypeBasedFusionCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IKBObjectWriteable;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.PropReferenceArray;
import de.dfki.step.kb.semantic.Type;
import de.dfki.step.util.LogUtils;

/**
 * Fuses two tokens specified by two patterns into a new token of a type that combines the origin
 * tokens. If several combinations of two matching tokens are possible, only the combination with
 * the newest token for each pattern is created. Tokens that were already used by this rule
 * are not considered anymore for future combinations.
 */
public class DeclarativeTypeBasedFusionRule extends Rule {
    private static final Logger log = LoggerFactory.getLogger(DeclarativeTypeBasedFusionRule.class);
    private static final int DEFAULT_PRIO = 5000;
    private static final String FUSION_TAG = "fusion";
	private Type _resultType;
	private String _prop1;
	private String _prop2;

	/**
	 * Constructs a new fusion rule that fuses two tokens of type t1 and t2 into a new
	 * token of type t1, where token 2 is inserted as a property value. Also works for inner
	 * types (concerning t1). Only tokens whose timestamps differ by less than the fusion interval are fused.
	 * @param t1 type of first origin token
	 * @param t2 type of second origin token
	 * @param propertyName of the property of token 1, in which token 2 should be inserted
	 * @param fusionInterval specifies the maximum time difference (in ms) between the two origin tokens
	 * Note: The type t1 needs to have a property with the given propertyName that matches type t2.
	 * @throws Exception if the conditions stated above are not satisfied
	 */
	public DeclarativeTypeBasedFusionRule(Type t1, Type t2, String propertyName, long fusionInterval, long maxTokenAge, KnowledgeBase kb) throws Exception {
		this.setPriority(DEFAULT_PRIO);
		if (t1 == null || t2 == null)
			throw new Exception("The types of the origin tokens cannot be null");

		IProperty prop = t1.getProperty(propertyName);
		if (prop == null)
			throw new Exception("Type 1 has no property with name " + propertyName);
		if (prop instanceof PropReference) {
			Type propType = ((PropReference) prop).getType();
			if (!t2.isInheritanceFrom(propType))
				throw new Exception("Type 2 does not inherit from the type of the property" + propertyName);
		}
		if (prop instanceof PropReferenceArray) {
			Type propType = ((PropReferenceArray) prop).getType();
			if (!t2.isInheritanceFrom(propType))
				throw new Exception("Type 2 does not inherit from the type of the property" + propertyName);
		}

		this._resultType = t1;
		this._prop1 = propertyName;
		Pattern p1 = new PatternBuilder(RRTypes.USER_INTENT, kb)
						.hasRecursiveType(t1.getName())
						.build();
		Pattern p2 = new PatternBuilder(t2.getName(), kb).build();
		this.setCondition(new DeclarativeTypeBasedFusionCondition(p1, p2, fusionInterval));
		this.getCondition().setMaxTokenAge(maxTokenAge);
		this.getTags().add(FUSION_TAG);
	}

	/**
	 * Constructs a new fusion rule that fuses two tokens matching p1 and p2 into a new
	 * token of type resultType. Only tokens whose timestamps differ by less than the fusion
	 * interval are fused.
	 * @param p1 pattern specifying first origin token
	 * @param p2 pattern specifying second origin token
	 * @param resultType type of the token resulting from the fusion
	 * @param fusionInterval specifies the maximum time difference (in ms) between the two origin tokens
	 * Note: p1 and p2 need to specify at least a root type for the corresponding token.
	 * resultType needs to have two properties that match the root types of the origin tokens.
	 * @throws Exception if the conditions stated above are not satisfied
	 */
	public DeclarativeTypeBasedFusionRule(Pattern p1, Pattern p2, Type resultType, long fusionInterval, long maxTokenAge) throws Exception {
		this.setPriority(DEFAULT_PRIO);
		if (resultType == null)
			throw new Exception("resultType cannot be null.");
		if (!p1.hasType() || !p2.hasType())
			throw new Exception("p1 and p2 need to specify a root type.");

		List<IProperty> props = resultType.getProperties();
		for (IProperty prop : props) {
			if (prop instanceof PropReference) {
				if (p1.getType().isInheritanceFrom(((PropReference) prop).getType()))
					this._prop1 = prop.getName();
				if (p2.getType().isInheritanceFrom(((PropReference) prop).getType()))
					this._prop2 = prop.getName();
			}
		}
		if (_prop1 == null || _prop2 == null)
			throw new Exception("resultType needs to have two properties that match the types of the origin tokens.");

		this._resultType = resultType;
		this.setCondition(new DeclarativeTypeBasedFusionCondition(p1, p2, fusionInterval));
		this.getCondition().setMaxTokenAge(maxTokenAge);
		this.getTags().add(FUSION_TAG);
	}

	   public DeclarativeTypeBasedFusionRule(Pattern p1, Pattern p2, Type resultType, long fusionInterval) throws Exception {
	       this(p1, p2, resultType, fusionInterval, Condition.DEFAULT_MAX_TOKEN_AGE);
	   }

	@Override
	public void onMatch(List<IToken[]> tokens, Board board) {
		for (IToken[] match : tokens) {
			IToken t1 = match[0];
			IToken t2 = match[1];
			
			IToken fusionResult;
	         try {
    			if (_prop2 != null) {
    				BasicToken basic = new BasicToken(t1.getKB());
    				basic.setType(this._resultType);
    				// simply reference origin tokens since tokens should not change anyway
    				basic.addAll(Map.of(_prop1, t1.getUUID().toString()));
    				basic.addAll(Map.of(_prop2, t2.getUUID().toString()));
    				fusionResult = basic;
    			} else {
    				fusionResult = t1.createTokenWithSameContent();
    				Pair<String, IKBObject> outer = findOuterObject(_resultType, t1);
    				// FIXME: what if inner is part of KBObject?
    				if (outer == null)
    					return;
    				IKBObjectWriteable innerObj = outer.getValue().getResolvedReference(outer.getKey());
    				IProperty prop = innerObj.getProperty(_prop1);
    				if (prop != null && prop instanceof PropReference) {
    					innerObj.setReference(_prop1, t2.getUUID());
    					fusionResult.setReference(outer.getKey(), innerObj);
    				} else if (prop != null && prop instanceof PropReferenceArray) {
    					innerObj.addReferenceToArray(_prop1, t2.getUUID());
    					fusionResult.setReference(outer.getKey(), innerObj);
    				}
    			}
    
    			fusionResult.setOriginTokens(List.of(t1, t2));
    			fusionResult.setProducer(this.getUUID());
    
    			t1.usedBy(this.getUUID());
    			t2.usedBy(this.getUUID());
    			t1.addResultingTokens(List.of(fusionResult), this.getUUID());
    			t2.addResultingTokens(List.of(fusionResult), this.getUUID());
    
    			board.addToken(fusionResult);
    			
    			// FIXME: temporary workaround
    			t1.setActive(false);
    
    			LogUtils.printDebugInfo("TOKEN AFTER FUSION", fusionResult);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Could not create fusion result.");
                
            }
		}
	}

	private Pair<String, IKBObject> findOuterObject(Type type, IKBObject tok) {
		if (tok == null)
			return null;
		for (IProperty prop : tok.getType().getProperties()) {
			if (prop instanceof PropReference || prop instanceof PropReferenceArray) {
				IKBObjectWriteable[] innerObjs = tok.getResolvedRefOrRefArray(prop.getName());
				// FIXME: what if there are multiple references
				for (IKBObjectWriteable innerObj : innerObjs) {
					if (innerObj.getType().isInheritanceFrom(type))
						return Pair.of(prop.getName(), tok);
					else {
						Pair<String, IKBObject> inner = findOuterObject(type, innerObj);
						if (inner != null)
							return inner;
					}
				}
			}
		}
		return null;
	}

}
