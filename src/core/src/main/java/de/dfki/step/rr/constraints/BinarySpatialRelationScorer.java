package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.rr.ReferenceResolver;
import de.dfki.step.rr.SpatialRR;
import de.dfki.step.util.LogUtils;

public class BinarySpatialRelationScorer extends RelationScorer {
    private static final Logger log = LoggerFactory.getLogger(BinarySpatialRelationScorer.class);
	private static final int DEFAULT_PRIO = 6000;
	private RRTypes.BinSpatRelation rel;
	private IKBObject relatumRef;

	public BinarySpatialRelationScorer(IKBObject constraint, KnowledgeBase kb) {
		super(constraint, kb);
		// TODO: make conversion from string to bin rel more flexible (e.g. case insensitive etc.)
		this.rel = RRTypes.BinSpatRelation.valueOf(constraint.getString("relation"));
		this.relatumRef = constraint.getResolvedReference("relatumReference");
		this.setPriority(DEFAULT_PRIO);
	}

	@Override
	public List<ObjectScore> updateScores(List<ObjectScore> scores) {
		ReferenceResolver rr = new SpatialRR(this.getKB());
		if (relatumRef == null)
			return new ArrayList<ObjectScore>();
		log.debug("RESOLVING RELATUM REFERENCE...");
		List<UUID> potentialRelatums = rr.resolveReference(relatumRef).getMostLikelyReferents();
		if (potentialRelatums == null || potentialRelatums.isEmpty())
			return new ArrayList<ObjectScore>();
		IKBObject relatum = this.getKB().getInstance(potentialRelatums.get(0));
		log.debug("RELATUM: " + relatum.getName());
		for (ObjectScore curScore : scores) {
			IKBObject obj = curScore.getObject();
			BinSpatComputer comp = new BinSpatComputer(obj, relatum, this.rel);
			double accScore = comp.computeScore();
			// TODO: replace 0 with value range close to 0
//			if (score == 0)
//				continue;
			curScore.accumulateScore((float) accScore);
		}
		// TODO: add relatumRef as text
		LogUtils.logScores("Totals after scoring BinSpatRel " + this.rel, scores);
		return scores;
	}

}
