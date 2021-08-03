package de.dfki.step.rr.constraints;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.util.LogUtils;

public abstract class ConstraintScorer {
	private int priority = 10000;
	private IKBObject constraint;
	private KnowledgeBase kb;
    private static final Logger log = LoggerFactory.getLogger(ConstraintScorer.class);

	public ConstraintScorer(IKBObject constraint, KnowledgeBase kb) {
		this.constraint = constraint;
		this.kb = kb;
	}

	public abstract List<ObjectScore> updateScores(List<ObjectScore> currentScores);

	public int getPriority() {
		return this.priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}

	public IKBObject getConstraint() {
		return this.constraint;
	}

	public KnowledgeBase getKB() {
		return this.kb;
	}

	public static ConstraintScorer getConstraintScorer(IKBObject constraint, KnowledgeBase kb) {
		try {
			if (constraint == null)
				return null;
			if (constraint.getType().isInheritanceFrom(RRTypes.BIN_SPAT_C)) 
				return new BinarySpatialRelationScorer(constraint, kb);
			if (constraint.getType().isInheritanceFrom(RRTypes.TYPE_C))
				return new TypeScorer(constraint, kb);
			if (constraint.getType().isInheritanceFrom(RRTypes.REGION_C))
				return new SpatialRegionScorer(constraint, kb);
			if (constraint.getType().isInheritanceFrom(RRTypes.GROUP_REL_C))
				return new GroupRelationScorer(constraint, kb);
			if (constraint.getType().isInheritanceFrom(RRTypes.POINTING_C))
				return new PointingScorer(constraint, kb);
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			log.warn("Exception while trying to instantiate constraint:" + e.getMessage());
			return null;
		}

	}

}
