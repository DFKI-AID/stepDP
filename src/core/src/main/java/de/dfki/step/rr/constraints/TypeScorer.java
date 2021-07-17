package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.List;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.Type;

public class TypeScorer extends ConstraintScorer {
	private KnowledgeBase kb;
	private Type type;

	public TypeScorer(IKBObject constraint, KnowledgeBase kb) {
		super(constraint);
		this.kb = kb;
	}

	@Override
	public List<ObjectScore> computeScores(List<IKBObject> objects) {
		List<ObjectScore> scores = new ArrayList<ObjectScore>();
		for (IKBObject obj : objects) {
			float score;
			if (obj.getType().isInheritanceFrom(type))
				score = 1;
			else
				score = 0;
			scores.add(new ObjectScore(obj.getUUID(), score));
		}
		return scores;
	}

}
