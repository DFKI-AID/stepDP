package de.dfki.step.rr;

import java.util.Comparator;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.rr.constraints.ObjectScore;

public class PrioComparator implements Comparator<IKBObject> {

	@Override
	public int compare(IKBObject o1, IKBObject o2) {
		Integer prio1 = o1.getInteger("priority");
		Integer prio2 = o2.getInteger("priority");
		return Integer.compare(prio1, prio2);
	}
}
