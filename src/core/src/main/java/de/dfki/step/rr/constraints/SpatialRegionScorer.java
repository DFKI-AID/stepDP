package de.dfki.step.rr.constraints;

import java.util.List;
import java.util.stream.Collectors;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.util.LogUtils;

public class SpatialRegionScorer extends ConstraintScorer {
	private static final int DEFAULT_PRIORITY = 5000;
	private RRTypes.SpatialRegion region;

	public SpatialRegionScorer(IKBObject constraint, KnowledgeBase kb) {
		super(constraint, kb);
		this.setPriority(DEFAULT_PRIORITY);
		// TODO: make conversion from string to region more flexible (e.g. case insensitive etc.)
		this.region = RRTypes.SpatialRegion.valueOf(constraint.getString("region"));
	}

	@Override
	public List<ObjectScore> computeScores(List<IKBObject> objects) {
		List<ObjectScore> scores = objects.stream().map(o -> new ObjectScore(o, 1)).collect(Collectors.toList());
		LogUtils.logScores("Scores for Region " + this.region, scores);
		return scores;
	}

}
