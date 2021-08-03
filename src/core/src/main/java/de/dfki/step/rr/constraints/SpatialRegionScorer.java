package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.RRTypes.Axis;
import de.dfki.step.util.LogUtils;

public class SpatialRegionScorer extends ConstraintScorer {
	private static final int DEFAULT_PRIORITY = 5000;
	private RRTypes.SpatialRegion region;
	// specificity (cf. Mast2016)
	private static final double c = 0.0001;

	public SpatialRegionScorer(IKBObject constraint, KnowledgeBase kb) {
		super(constraint, kb);
		this.setPriority(DEFAULT_PRIORITY);
		// TODO: make conversion from string to region more flexible (e.g. case insensitive etc.)
		// TODO: catch exception if no matching value in enum
		this.region = RRTypes.SpatialRegion.valueOf(constraint.getString("region"));
	}

	@Override
	public List<ObjectScore> updateScores(List<ObjectScore> scores) {
		List<IKBObject> objects = this.getKB().getInstancesOfType(this.getKB().getType(RRTypes.SPAT_REF_TARGET));
		Double prototype = SpatialRegionComputer.computePrototype(objects, this.region);
		for (ObjectScore curScore : scores) {
			PhysicalObject physObj = new PhysicalObject(curScore.getObject());
			Double current = physObj.getPositionOn(this.region.getAxis());
			Double diff = Math.abs(prototype - current);
			Double accScore = Math.pow(Math.E, -c * diff * diff);
			curScore.accumulateScore(accScore.floatValue());
		}
		LogUtils.logScores("Totals after scoring Region " + this.region, scores);
		return scores;
	}


}
