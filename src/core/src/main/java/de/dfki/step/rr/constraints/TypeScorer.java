package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.List;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.semantic.Type;

public class TypeScorer extends ConstraintScorer {
	private static final int DEFAULT_PRIORITY = 1000;
	private KnowledgeBase kb;
	private Type type;

	public TypeScorer(IKBObject constraint, KnowledgeBase kb) {
		super(constraint);
		this.setPriority(DEFAULT_PRIORITY);
		this.kb = kb;
		String refType= constraint.getString("refType");
		this.type = kb.getType(refType);
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
