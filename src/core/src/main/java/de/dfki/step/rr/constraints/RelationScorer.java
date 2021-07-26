package de.dfki.step.rr.constraints;

import java.util.List;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;

public class RelationScorer extends ConstraintScorer {

	public RelationScorer(IKBObject constraint, KnowledgeBase kb) {
		super(constraint, kb);
	}

	@Override
	public List<ObjectScore> computeScores(List<IKBObject> objects) {
		// TODO Auto-generated method stub
		return null;
	}

}
