package de.dfki.step.rr.constraints;

import java.util.List;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;

public abstract class ConstraintScorer {
	private int priority = 10000;
	private IKBObject constraint;

	public ConstraintScorer(IKBObject constraint) {
		this.constraint = constraint;
	}

	public abstract List<ObjectScore> computeScores(List<IKBObject> objects);

	public int getPriority() {
		return this.priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}

	public static ConstraintScorer getConstraintScorer(IKBObject constraint, KnowledgeBase kb) {
		if (constraint.getType().isInheritanceFrom(RRTypes.BIN_SPAT_C)) 
			return new BinarySpatialRelationScorer(constraint, kb);
		if (constraint.getType().isInheritanceFrom(RRTypes.TYPE_C))
			return new TypeScorer(constraint, kb);
		else
			return null;
	}
}
