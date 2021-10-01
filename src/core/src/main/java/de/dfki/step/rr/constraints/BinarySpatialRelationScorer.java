package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
	private IKBObject speaker;

	public BinarySpatialRelationScorer(IKBObject constraint, IKBObject speaker, KnowledgeBase kb) throws Exception {
		super(constraint, kb);
		// TODO: make conversion from string to bin rel more flexible (e.g. case insensitive etc.)
		this.rel = RRTypes.BinSpatRelation.valueOf(constraint.getString("relation"));
		this.relatumRef = constraint.getResolvedReference("relatumReference");
		this.speaker = speaker;
		if (this.relatumRef == null || this.speaker == null)
			throw new Exception("Binary Spatial Relation Constraint needs relatumRef and speaker.");
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
		// FIXME: support also references with multiple referents?
		IKBObject relatum = this.getKB().getInstance(potentialRelatums.get(0));
		log.debug("RELATUM: " + relatum.getName());
		if (rel.isProjective()) {
			for (ObjectScore curScore : scores) {
				IKBObject obj = curScore.getObject();
				ProjectiveBinSpatComputer comp = new ProjectiveBinSpatComputer(speaker, obj, relatum, rel);
				double accScore = comp.computeScore();
				// TODO: replace 0 with value range close to 0
//				if (score == 0)
//					continue;
				curScore.accumulateScore((float) accScore);
			}
		} else {
			List<IKBObject> potentialObjects = scores.stream().map(ObjectScore::getObject).collect(Collectors.toList());
			NonProjectiveBinSpatComputer comp = new NonProjectiveBinSpatComputer(relatum, rel, potentialObjects);
			List<ObjectScore> newScores = comp.computeScores();
			scores = ObjectScore.accumulateScores(scores, newScores);
		}

		// TODO: add relatumRef as text
		LogUtils.logScores("Totals after scoring BinSpatRel " + this.rel, scores);
		return scores;
	}

}
