package de.dfki.step.rr;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import de.dfki.step.rr.constraints.ObjectScore;
import de.dfki.step.rr.constraints.ScoreReverseComparator;

public class ResolutionResult {
	// sorted 
	private List<ObjectScore> potentialReferents;


	public ResolutionResult(List<ObjectScore> potentialReferents) {
		this.potentialReferents = potentialReferents.stream()
									.sorted(new ScoreReverseComparator())
									.collect(Collectors.toList());
	}

	public float getMaxScore() {
		if (this.potentialReferents.isEmpty())
			return 0;
		else
			return this.potentialReferents.get(0).getScore();
	}
	
	public List<UUID> getMostLikelyReferents(){
		float maxScore = this.getMaxScore();
		return this.potentialReferents.stream()
									  .filter(s -> s.getScore() == maxScore)
									  .map(s -> s.getUUID())
									  .collect(Collectors.toList());
	};
}
