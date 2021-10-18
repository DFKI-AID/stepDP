package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IKBObjectWriteable;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.rr.RRConfigParameters;

public class NonProjectiveBinSpatComputer {
	private IKBObject ro;
	private RRTypes.BinSpatRelation rel;
	private List<IKBObject> potentialIOs;
	private RRConfigParameters config;
	
	public NonProjectiveBinSpatComputer(IKBObject ro, RRTypes.BinSpatRelation rel, List<IKBObject> potentialIOs, RRConfigParameters config) {
		this.config = config;
		this.ro = ro;
		this.rel = rel;
		this.potentialIOs = potentialIOs;
	}

	public List<ObjectScore> computeScores() {
		if (rel == null || ro.getType() == null || !ro.getType().isInheritanceFrom(RRTypes.CONTAINER))
			return Collections.EMPTY_LIST;
		List<IKBObjectWriteable> matches = Collections.EMPTY_LIST;
		switch (rel) {
		case in:
			matches = findAllContainedItems(ro);
			break;
		case on:
			// FIXME: temporary workaround to make expressions like "on the second shelf" work
			matches = findAllContainedItems(ro);
			break;
		}
		List<ObjectScore> scores = matches.stream().map(m -> new ObjectScore(m, 1.0f)).collect(Collectors.toList());
		return scores;
	}

	private List<IKBObjectWriteable> findAllContainedItems(IKBObject obj){
		IKBObjectWriteable[] containedItems = obj.getResolvedReferenceArray("contains");
		List<IKBObjectWriteable> matches = new ArrayList<IKBObjectWriteable>(Arrays.asList(containedItems));
		for (IKBObject inner : containedItems) {
			if (inner.getType() != null && inner.getType().isInheritanceFrom(RRTypes.CONTAINER)) {
				List<IKBObjectWriteable> innerItems = findAllContainedItems(inner);
				matches.addAll(innerItems);
			}
		}
		matches.retainAll(potentialIOs);
		return matches;
	}

}
