package de.dfki.step.rr;

import java.util.Comparator;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.rr.constraints.ConstraintScorer;
import de.dfki.step.rr.constraints.ObjectScore;

public class PrioComparator implements Comparator<ConstraintScorer> {

	@Override
	public int compare(ConstraintScorer s1, ConstraintScorer s2) {
		Integer prio1 = s1.getPriority();
		Integer prio2 = s2.getPriority();
		return Integer.compare(prio1, prio2);
	}
}
