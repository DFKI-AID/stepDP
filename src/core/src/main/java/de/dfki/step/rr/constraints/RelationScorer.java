package de.dfki.step.rr.constraints;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;

public abstract class RelationScorer extends ConstraintScorer {

	public RelationScorer(IKBObject constraint, KnowledgeBase kb) {
		super(constraint, kb);
	}

}
