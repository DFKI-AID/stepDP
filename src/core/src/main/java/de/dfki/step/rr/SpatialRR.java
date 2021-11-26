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

	private boolean isVisible(IKBObject o, RRConfigParameters config) {
		Float visibility = o.getFloat("visibility");
		return (visibility != null && visibility >= config.VISIBILITY_THRESHOLD);
	}
	
	@Override
	public ResolutionResult resolveReference(IKBObject reference, RRConfigParameters config) {
		IKBObject speaker = reference.getResolvedReference("speaker");
		Integer cardinality = reference.getInteger("cardinality");
		// retrieve all objects that could be referents from the kb
		List<IKBObject> potentialReferents = this.kb.getInstancesOfType(this.kb.getType(RRTypes.SPAT_REF_TARGET));
		// ignore objects of which a large part is not visible (below visibility threshold)
		potentialReferents = potentialReferents.stream().filter(o -> isVisible(o, config)).collect(Collectors.toList());
		List<ObjectScore> currentScores = ObjectScore.initializeScores(potentialReferents);
		
		// construct constraint scorers for the constraint types provided in the reference and sort by prio
		IKBObject[] constraintArray = reference.getResolvedReferenceArray("constraints");
		if (constraintArray == null)
			return new ResolutionResult(currentScores);
		List<IKBObject> constraints = new ArrayList<IKBObject>(Arrays.asList(constraintArray));
		List<ConstraintScorer> scorers = constraints.stream()
											.map(c -> ConstraintScorer.getConstraintScorer(c, speaker, kb, cardinality, config))
											.filter(c -> c!= null)
											.collect(Collectors.toList());
		scorers.sort(new PrioComparator());

		// evaluate constraints and update scores accordingly
		for (ConstraintScorer scorer : scorers) {
			currentScores = scorer.updateScores(currentScores)
								  .stream()
								  .filter(s -> s.getScore() != 0)
								  .collect(Collectors.toList());
		}

		// return the final object confidences
		ResolutionResult result = new ResolutionResult(currentScores);
		return result;
 	}

}
