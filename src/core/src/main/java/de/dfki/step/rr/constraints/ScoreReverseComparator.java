package de.dfki.step.rr.constraints;

import java.util.Comparator;

public class ScoreReverseComparator implements Comparator<ObjectScore> {

	@Override
	public int compare(ObjectScore s1, ObjectScore s2) {
		return Float.compare(s2.getScore(), s1.getScore());
	}

}
