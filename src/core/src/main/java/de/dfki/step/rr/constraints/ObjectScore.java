package de.dfki.step.rr.constraints;

import java.util.Comparator;
import java.util.UUID;

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
}
