package de.dfki.step.blackboard.rules;

import java.util.List;
import java.util.UUID;

import de.dfki.step.blackboard.Board;
import de.dfki.step.blackboard.IToken;
import de.dfki.step.blackboard.KBToken;
import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.ValueReplacement;
import de.dfki.step.blackboard.conditions.PatternCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IKBObjectWriteable;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;
import de.dfki.step.rr.ResolutionResult;
import de.dfki.step.rr.SpatialRR;

public class SpatialReferenceResolutionRule extends Rule {
	KnowledgeBase kb;
	SpatialRR rr;

	public SpatialReferenceResolutionRule(KnowledgeBase kb) throws Exception {
		this.kb = kb;
		this.rr = new SpatialRR(kb);

		PatternBuilder builder = new PatternBuilder(RRTypes.USER_INTENT, kb);
		builder.hasRecursiveType(RRTypes.SPAT_REF);
		Pattern p = builder.build();
		this.setCondition(new PatternCondition(p));
	}
	
	@Override
	public void onMatch(List<IToken[]> tokens, Board board) {
        if(tokens == null || tokens.size() == 0)
            return;

        for (IToken[] tArray : tokens) {
        	IToken t = tArray[0];
        	t.usedBy(this.getUUID());
        	ValueReplacement replace = new ValueReplacement();
    		IKBObject resolved = findAndResolveReferences(t, replace);
    		IToken newToken;
    		if (resolved != null)
    			newToken = new KBToken(kb, resolved);
    		else
    			newToken = t.createCopyAndReplaceParts(replace);
    		this.kb.getBlackboard().addToken(newToken);
        }
	}

	// replaces spatial references with the most likely referent object
	private IKBObject findAndResolveReferences(IKBObject obj, ValueReplacement replace) {
		IKBObject referent = null;
		if (RRTypes.isSpatialReference(obj, kb)) {
			referent = resolveReference(obj);
		}
		for (IProperty prop : obj.getType().getProperties()) {
			if (!(prop instanceof PropReference))
				continue;
			IKBObject innerObject = obj.getResolvedReference(prop.getName());
			if (innerObject == null)
				continue;
			ValueReplacement innerReplace = replace.getInnerReplacement(prop.getName());
			IKBObject innerReferent = findAndResolveReferences(innerObject, innerReplace);
			if (innerReferent != null)
				replace.replaceValue(prop.getName(), innerReferent);
		}
		return referent;
	}
	
	private IKBObject resolveReference(IKBObject obj) {
		ResolutionResult result = this.rr.resolveReference(obj);
		List<UUID> referents = result.getMostLikelyReferents();
		// TODO: handle empty referents, low confidence, ambiguity etc.
		if (referents.isEmpty()) 
			return null;
	    IKBObject referent = kb.getInstance(referents.get(0));
	    return referent;
	}
}
