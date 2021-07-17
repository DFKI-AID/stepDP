package de.dfki.step.rr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.rr.constraints.ConstraintScorer;
import de.dfki.step.rr.constraints.ObjectScore;

public class SpatialRR implements ReferenceResolver {
	private KnowledgeBase kb;
	
	public SpatialRR(KnowledgeBase kb) {
		this.kb = kb;
	}
	
	@Override
	public ResolutionResult resolveReference(IKBObject reference) {
		List<IKBObject> potentialReferents = this.kb.getInstancesOfType(this.kb.getType(RRTypes.SPAT_REF_TARGET));
		List<ObjectScore> total = ObjectScore.initializeScores(potentialReferents);
		IKBObject[] constraintArray = reference.getResolvedReferenceArray("constraints");
		if (constraintArray == null)
			return new ResolutionResult(total);
		List<IKBObject> constraints = List.of(constraintArray);
		constraints.sort(new PrioComparator());
		for (IKBObject c : constraints) {
			ConstraintScorer scorer = ConstraintScorer.getConstraintScorer(c, kb);
			List<ObjectScore> current = scorer.computeScores(potentialReferents);
			total = ObjectScore.accumulateScores(total, current);
			// TODO prune object with score 0
		}
		ResolutionResult result = new ResolutionResult(total);
		return result;
 	}


}
