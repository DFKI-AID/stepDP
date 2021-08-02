package de.dfki.step.rr;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.rr.constraints.ObjectScore;

public class ObjectGroup {
	private static final float CONFIDENCE_THRESHOLD = 0.7f;
	private List<ObjectScore> scores;
	private float groupConfidence;

	public ObjectGroup(List<ObjectScore> scores) {
		this.scores = scores;
		this.groupConfidence = computeGroupConfidence();
	}

	private float computeGroupConfidence() {
		Float total = scores
						.stream()
						.map(s -> s.getScore())
						.reduce(1.0f, (s1,s2) -> s1 * s2);
		return total.floatValue();
	}

	public static List<ObjectGroup> findGroupCandidates(List<ObjectScore> scores, Integer minCardinality, Integer maxCardinality){
		// TODO: find smarter way to do this; also consider cardinality
		List<ObjectScore> members = scores
									.stream()
									.filter(s -> s.getScore() >= CONFIDENCE_THRESHOLD)
									.collect(Collectors.toList());
		if (members.isEmpty())
			return Collections.EMPTY_LIST;
		else
			return List.of(new ObjectGroup(members));
	}

	public int getSize() {
		return this.scores.size();
	}

	public List<IKBObject> getObjects() {
		return scores.stream().map(s -> s.getObject()).collect(Collectors.toList());
	}

	public List<ObjectScore> getScores() {
		return scores;
	}

	public float getGroupConfidence() {
		return this.groupConfidence;
	}

}
