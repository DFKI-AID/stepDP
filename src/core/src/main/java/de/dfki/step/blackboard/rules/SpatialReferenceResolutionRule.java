package de.dfki.step.blackboard.rules;

import java.util.List;

import de.dfki.step.blackboard.Board;
import de.dfki.step.blackboard.IToken;
import de.dfki.step.blackboard.Rule;
import de.dfki.step.blackboard.conditions.PatternCondition;
import de.dfki.step.blackboard.patterns.Pattern;
import de.dfki.step.blackboard.patterns.PatternBuilder;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.PropReference;

public class SpatialReferenceResolutionRule extends Rule {
	KnowledgeBase kb;

	public SpatialReferenceResolutionRule(KnowledgeBase kb) throws Exception {
		this.kb = kb;

		PatternBuilder builder = new PatternBuilder(RRTypes.USER_INTENT, kb);
		builder.hasRecursiveType(RRTypes.SPAT_REF);
		Pattern p = builder.build();
		this.setCondition(new PatternCondition(p));
	}
	
	@Override
	public void onMatch(List<IToken[]> tokens, Board board) {
		// scan for references
		// for each reference
		
			// resolve reference
			// handle result
		// create new token where references are replaced by referents
        if(tokens == null || tokens.size() == 0)
            return;

        for (IToken[] tArray : tokens) {
        	IToken t = tArray[0];
        	t.usedBy(this.getUUID());
    		findAndResolveReferences(t.getCopy());
        }
	}

	private void findAndResolveReferences(IToken obj) {
		if (obj.getType().isInheritanceFrom(kb.getType(RRTypes.REFERENCE)))
			resolveReference(obj);
		for (IProperty prop : obj.getType().getProperties()) {
			if (!(prop instanceof PropReference))
				continue;
			IKBObject innerObject = obj.getResolvedReference(prop.getName());
			if (innerObject == null || !(innerObject instanceof IToken))
				continue;
			findAndResolveReferences((IToken) innerObject);
		}
	}
	
	private void resolveReference(IKBObject obj) {
		
	}
}
