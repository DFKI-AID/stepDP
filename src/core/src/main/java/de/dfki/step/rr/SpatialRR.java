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

	private boolean isVisible(IKBObject o) {
		Float visibility = o.getFloat("visibility");
		return (visibility != null && visibility >= RRConfigParameters.VISIBILITY_THRESHOLD);
	}
	
	@Override
	public ResolutionResult resolveReference(IKBObject reference) {
		IKBObject speaker = reference.getResolvedReference("speaker");
		List<IKBObject> potentialReferents = this.kb.getInstancesOfType(this.kb.getType(RRTypes.SPAT_REF_TARGET));
		potentialReferents = potentialReferents.stream().filter(o -> isVisible(o)).collect(Collectors.toList());
		List<ObjectScore> currentScores = ObjectScore.initializeScores(potentialReferents);
		IKBObject[] constraintArray = reference.getResolvedReferenceArray("constraints");
		if (constraintArray == null)
			return new ResolutionResult(currentScores);
		List<IKBObject> constraints = new ArrayList<IKBObject>(Arrays.asList(constraintArray));
		List<ConstraintScorer> scorers = constraints.stream()
											.map(c -> ConstraintScorer.getConstraintScorer(c, speaker, kb))
											.filter(c -> c!= null)
											.collect(Collectors.toList());
		scorers.sort(new PrioComparator());
		for (ConstraintScorer scorer : scorers) {
			currentScores = scorer.updateScores(currentScores)
								  .stream()
								  .filter(s -> s.getScore() != 0)
								  .collect(Collectors.toList());
		}
		ResolutionResult result = new ResolutionResult(currentScores);
		return result;
 	}

}
