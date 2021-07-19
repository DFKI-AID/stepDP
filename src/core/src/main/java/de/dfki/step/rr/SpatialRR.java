package de.dfki.step.rr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
		List<IKBObject> constraints = new ArrayList<IKBObject>(Arrays.asList(constraintArray));
		List<ConstraintScorer> scorers = constraints.stream()
											.map(c -> ConstraintScorer.getConstraintScorer(c, kb))
											.filter(c -> c!= null)
											.collect(Collectors.toList());
		scorers.sort(new PrioComparator());
		for (ConstraintScorer scorer : scorers) {
			List<ObjectScore> current = scorer.computeScores(potentialReferents);
			total = ObjectScore.accumulateScores(total, current);
			potentialReferents = pruneCandidates(potentialReferents, total);
			total = total.stream().filter(s -> s.getScore() != 0).collect(Collectors.toList());
		}
		ResolutionResult result = new ResolutionResult(total);
		return result;
 	}

	private List<IKBObject> pruneCandidates(List<IKBObject> candidates, List<ObjectScore> total) {
		List<UUID> dropouts = total.stream()
				.filter(s -> s.getScore() != 0)
				.map(s -> s.getUUID())
				.collect(Collectors.toList());
		// TODO: make this more efficient?
		candidates = candidates.stream().filter(r -> dropouts.contains(r.getUUID())).collect(Collectors.toList());
		return candidates;
	}

}
