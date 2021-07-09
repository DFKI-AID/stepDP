package de.dfki.step.rr.constraints;

import de.dfki.step.kb.IKBObject;

public abstract class ConstraintScorer {
	private int priority = 10000;
	private IKBObject constraint;

	public ConstraintScorer(IKBObject constraint) {
		
	}

	public abstract ObjectScores computeScores();

	public int getPriority() {
		return this.priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
