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
		this.region = RRTypes.SpatialRegion.valueOf(constraint.getString("region"));
	}

	@Override
	public List<ObjectScore> computeScores(List<IKBObject> objects) {
		List<ObjectScore> scores = new ArrayList<ObjectScore>();
		Double prototype = this.computePrototype();
		for (IKBObject obj : objects) {
			PhysicalObject physObj = new PhysicalObject(obj);
			Double current = physObj.getPositionOn(this.region.getAxis());
			Double diff = Math.abs(prototype - current);
			Double score = Math.pow(Math.E, -c * diff * diff);
			scores.add(new ObjectScore(obj, score.floatValue())); 
		}
		LogUtils.logScores("Scores for Region " + this.region, scores);
		return scores;
	}

	private Double computePrototype() {
		Axis axis = this.region.getAxis();
	    Stream<Double> positions = this.getKB().getInstancesOfType(this.getKB().getType(RRTypes.SPAT_REF_TARGET))
	    	      						.stream()
	    	      						// FIXME: actually it should be the outermost point not the center point
	    	      						.map(o -> new PhysicalObject(o).getPositionOn(axis));
	    Double prototype;
	    if (this.region.positive())
	    	prototype = positions
	    	      		.max(Comparator.naturalOrder())
	    	      		.orElseThrow(NoSuchElementException::new);
	    else prototype = positions
	    				.min(Comparator.naturalOrder())
	    				.orElseThrow(NoSuchElementException::new);
	    return prototype;
	}

	private Function<PhysicalObject, Double> getPositionOn(Axis axis) {
		switch (axis) {
		case X:
			return PhysicalObject::getX;
		case Y:
			return PhysicalObject::getY;
		case Z:
			return PhysicalObject::getZ;
		default:
			throw new IllegalStateException("not a valid axis");
		}
	}

}
