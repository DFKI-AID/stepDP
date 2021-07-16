package de.dfki.step.rr;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;

public class SpatialRR implements ReferenceResolver {
	private KnowledgeBase kb;
	
	public SpatialRR(KnowledgeBase kb) {
		this.kb = kb;
	}
	
	@Override
	public ResolutionResult resolveReference(IKBObject reference) {
		// TODO Auto-generated method stub
		return null;
	}

}
