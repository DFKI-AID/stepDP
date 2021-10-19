package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.kb.RRTypes.Axis;
import de.dfki.step.kb.SpatialRegion;
import de.dfki.step.rr.RRConfigParameters;
import de.dfki.step.util.LogUtils;

/**
 * 
 * Computation of confidences for spatial regions based on the following publication:
 * Vivien Mast, Zoe Falomir, and Diedrich Wolter. 2016. Probabilistic reference and grounding with
 * PRAGR for dialogues with robots. Journal of Experimental and Theoretical Artificial Intelligence 28,
 * 5 (2016), 889–911
 *
 */
public class SpatialRegionScorer extends ConstraintScorer {
    private static final Logger log = LoggerFactory.getLogger(SpatialRegionScorer.class);
	private static final int DEFAULT_PRIORITY = 6000;
	private SpatialRegion region;
	private double C;
	private RRConfigParameters config;

	public SpatialRegionScorer(IKBObject constraint, KnowledgeBase kb,  RRConfigParameters config) {
		super(constraint, kb);
		this.config = config;
		this.C = config.SPATREG_C;
		this.setPriority(DEFAULT_PRIORITY);
		// TODO: make conversion from string to region more flexible (e.g. case insensitive etc.)
		// TODO: catch exception if no matching value in enum
		try {
			this.region = SpatialRegion.valueOf(constraint.getString("region"));
		} catch (Exception e) {
			log.error("invalid region" + constraint.getString("region"));
		}
	}

	@Override
	public List<ObjectScore> updateScores(List<ObjectScore> scores) {
		if (this.region == null)
				return scores;
		List<IKBObject> objects = this.getKB().getInstancesOfType(this.getKB().getType(RRTypes.SPAT_REF_TARGET));
		Double prototype = SpatialRegionComputer.computePrototype(objects, this.region, config);
		for (ObjectScore curScore : scores) {
			PhysicalObject physObj = new PhysicalObject(curScore.getObject());
			Double current = physObj.getPositionOn(this.region.getAxis());
			Double diff = Math.abs(prototype - current);
			Double accScore = Math.pow(Math.E, -C * diff * diff);
			curScore.accumulateScore(accScore.floatValue());
		}
		LogUtils.logScores("Totals after scoring Region " + this.region, scores);
		return scores;
	}

<<<<<<< adina-ma-reference-resolution
=======
	@Override
	public String getDirection() {
		if (this.region == null)
			return null;
		else
			return this.region.direction;
	}

>>>>>>> b046302 some adjustments

}
