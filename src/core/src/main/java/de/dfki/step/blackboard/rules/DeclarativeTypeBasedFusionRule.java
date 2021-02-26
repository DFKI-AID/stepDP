package de.dfki.step.blackboard.rules;

import java.util.List;
import java.util.Map;

import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.Board;
import de.dfki.step.blackboard.IToken;
import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.conditions.DeclarativeTypeBasedFusionCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.kb.semantic.Type;

/**
 * Fuses two tokens specified by two patterns into a new token of a type that combines the origin
 * tokens. If several combinations of two matching tokens are possible, only the combination with
 * the newest token for each pattern is created. Tokens that were already used by this rule
 * are not considered anymore for future combinations.
 */
public class DeclarativeTypeBasedFusionRule extends Rule {
    private static final int DEFAULT_PRIO = 5000;
    private static final String FUSION_TAG = "fusion";
	private Type _resultType;
	private String _prop1;
	private String _prop2;
	
	/**
	 * Constructs a new fusion rule that fuses two tokens matching p1 and p2 into a new
	 * token of type resultType. Only tokens whose timestamps differ by less than the fusion
	 * interval are fused.
	 * @param p1 pattern specifying first origin token
	 * @param p2 pattern specifying second origin token
	 * @param resultType type of the token resulting from the fusion
	 * @param fusionInterval specifies the maximum time difference between the two origin tokens
	 * Note: p1 and p2 need to specify at least a root type for the corresponding token.
	 * resultType needs to have two properties that match the root types of the origin tokens.
	 * @throws Exception if the conditions stated above are not satisfied
	 */
	public DeclarativeTypeBasedFusionRule(Pattern p1, Pattern p2, Type resultType, long fusionInterval) throws Exception {
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
		this.getTags().add(FUSION_TAG);
	}

	@Override
	public void onMatch(List<IToken[]> tokens, Board board) {
		for (IToken[] match : tokens) {
			IToken t1 = match[0];
			IToken t2 = match[1];
			
			BasicToken fusionResult = new BasicToken(t1.getKB());
			fusionResult.setType(this._resultType);
			// simply reference origin tokens since tokens should not change anyway
			fusionResult.addAll(Map.of(_prop1, t1));
			fusionResult.addAll(Map.of(_prop2, t2));
			fusionResult.setOriginTokens(List.of(t1, t2));
			fusionResult.setProducer(this.getUUID());

			t1.usedBy(this.getUUID());
			t2.usedBy(this.getUUID());
			t1.addResultingTokens(List.of(fusionResult), this.getUUID());
			t2.addResultingTokens(List.of(fusionResult), this.getUUID());

			board.addToken(fusionResult);
		}
	}

}
