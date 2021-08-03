package de.dfki.step.rr.constraints;

import java.util.Arrays;
import java.util.List;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.util.LogUtils;

public class PointingScorer extends ConstraintScorer {
	private static final int DEFAULT_PRIORITY = 1000;
	private List<String> objectNames;

	public PointingScorer(IKBObject constraint, KnowledgeBase kb) {
		super(constraint, kb);
		this.setPriority(DEFAULT_PRIORITY);
		this.objectNames = Arrays.asList(constraint.getStringArray("objectNames"));
	}

	@Override
	public List<ObjectScore> updateScores(List<ObjectScore> scores) {
		for (ObjectScore curScore : scores) {
			if (!(objectNames.contains(curScore.getObject().getName())))
				curScore.setScore(0);
		}
		LogUtils.logScores("Scores for pointing constraint", scores);
		return scores;
	}

}
