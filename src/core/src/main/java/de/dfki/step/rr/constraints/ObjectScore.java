package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.dfki.step.kb.IKBObject;

public class ObjectScore {
	private UUID uuid;
	private float score;
	
	public ObjectScore(UUID uuid, float score) {
		this.uuid = uuid;
		this.score = score;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public float getScore() {
		return this.score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public static List<ObjectScore> initializeScores(List<IKBObject> potentialReferents) {
		List<ObjectScore> scores = new ArrayList<ObjectScore>();
		float individualScore = 1 / potentialReferents.size();
		for (IKBObject obj : potentialReferents) {
			scores.add(new ObjectScore(obj.getUUID(), individualScore));
		}
		return scores;
	}

	public static List<ObjectScore> accumulateScores(List<ObjectScore> totalList, List<ObjectScore> accumulateList){
		Map<UUID, ObjectScore> accumulateMap = accumulateList.stream().collect(Collectors.toMap(ObjectScore::getUUID, Function.identity()));
		for (ObjectScore total : totalList) {
			ObjectScore accumulate = accumulateMap.get(total.getUUID());
			float oldS = total.getScore();
			float accumulateS = accumulate.getScore();
			float newS = (accumulateS == 0) ? 0 : (oldS * accumulateS);
			total.setScore(newS);
		}
		return totalList;
	}
}
