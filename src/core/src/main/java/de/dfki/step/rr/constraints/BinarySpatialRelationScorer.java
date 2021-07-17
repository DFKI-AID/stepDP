package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.rr.ReferenceResolver;
import de.dfki.step.rr.SpatialRR;

public class BinarySpatialRelationScorer extends RelationScorer {
	private static final int DEFAULT_PRIO = 6000;
	private RRTypes.BinaryRelation rel;
	private IKBObject relatumRef;
	private KnowledgeBase kb;

	public BinarySpatialRelationScorer(IKBObject constraint, KnowledgeBase kb) {
		super(constraint);
		// TODO: make conversion from string to bin rel more flexible (e.g. case insensitive etc.)
		this.rel = RRTypes.BinaryRelation.valueOf(constraint.getString("relation"));
		this.relatumRef = constraint.getResolvedReference("relatumReference");
		this.setPriority(DEFAULT_PRIO);
		this.kb = kb;
	}

	@Override
	public List<ObjectScore> computeScores(List<IKBObject> objects) {
		List<ObjectScore> scores = new ArrayList<ObjectScore>();
		ReferenceResolver rr = new SpatialRR(kb);
		List<UUID> potentialRelatums = rr.resolveReference(relatumRef).getMostLikelyReferents();
		if (potentialRelatums == null || potentialRelatums.isEmpty())
			return scores;
		IKBObject relatum = this.kb.getInstance(potentialRelatums.get(0));
		for (IKBObject obj : objects) {
			float score = computeScore(obj, relatum);
			// TODO: replace 0 with value range close to 0
			if (score == 0)
				continue;
			scores.add(new ObjectScore(obj.getUUID(), score));
		}
		return scores;
	}

	private float computeScore(IKBObject io, IKBObject ro) {
		// TODO: implement
		return 1;
	}

}
