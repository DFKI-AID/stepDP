package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.IKBObjectWriteable;
import de.dfki.step.kb.RRTypes;
import de.dfki.step.rr.RRConfigParameters;

public class NonProjectiveBinSpatComputer {
    private static final Logger log = LoggerFactory.getLogger(NonProjectiveBinSpatComputer.class);
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
		List<IKBObject> matches = Collections.EMPTY_LIST;
		switch (rel) {
		case in:
			matches = findAllContainedItems(ro);
			break;
		case on:
		    if (config.ON_EQUALS_IN_TYPES.contains(ro.getType().getName())){
		          matches = findAllContainedItems(ro);
		          break;
		    } else {
		        log.warn("Relation \"on\" is not supported for relatum objects of type " + ro.getType());
		    }
		}
		List<ObjectScore> scores = matches.stream().map(m -> new ObjectScore(m, 1.0f)).collect(Collectors.toList());
		return scores;
	}

	private List<IKBObject> findAllContainedItems(IKBObject obj){
		IKBObject[] containedItems = obj.getResolvedReferenceArray("contains");
        if (containedItems == null)
            return Collections.EMPTY_LIST;
        List<IKBObject> matches = new ArrayList<IKBObject>(Arrays.asList(containedItems));
        for (IKBObject inner : containedItems) {
            if (inner == null) {
                matches.remove(inner);
                continue;
            }
			if (inner.getType() != null && inner.getType().isInheritanceFrom(RRTypes.CONTAINER)) {
				List<IKBObject> innerItems = findAllContainedItems(inner);
				matches.addAll(innerItems);
			}
		}
		matches.retainAll(potentialIOs);
		return matches;
	}

}
