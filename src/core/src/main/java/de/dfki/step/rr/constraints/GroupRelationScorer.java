package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.SpatialRegion;
import de.dfki.step.kb.semantic.Type;
import de.dfki.step.rr.ObjectGroup;
import de.dfki.step.rr.RRConfigParameters;
import de.dfki.step.util.LogUtils;

public class GroupRelationScorer extends RelationScorer {
    private static final Logger log = LoggerFactory.getLogger(GroupRelationScorer.class);
	private static final int DEFAULT_PRIO = 10000;
	private SpatialRegion relation;
	private Integer ordinality;
	private Integer cardinality;
	private RRConfigParameters config;

	public GroupRelationScorer(IKBObject constraint, KnowledgeBase kb, Integer cardinality,  RRConfigParameters config) {
		super(constraint, kb);
		// TODO: make conversion from string to rel more flexible (e.g. case insensitive etc.)
		this.config = config;
		this.cardinality = cardinality;
		if (constraint.isSet("relation")) {
			try {
				this.relation = SpatialRegion.valueOf(constraint.getString("relation"));
			} catch (Exception e) {
				log.error("invalid group relation " + constraint.getString("relation"));
			}
		}

		this.ordinality = constraint.getInteger("ordinality");
		this.setPriority(DEFAULT_PRIO);
	}

	@Override
	public List<ObjectScore> updateScores(List<ObjectScore> scores) {
		List<ObjectGroup> potentialGroups = ObjectGroup.findGroupCandidates(scores, ordinality, -1);
		if (potentialGroups == null || potentialGroups.isEmpty()) {
			log.debug("No object groups found for group relation.");
			return Collections.EMPTY_LIST;
		}
		// TODO: consider also other potential groups
		Optional<ObjectGroup> bestGroup = potentialGroups
			      				.stream()
			      				.min(Comparator.comparing(ObjectGroup::getGroupConfidence));
	    if (!bestGroup.isPresent())
	    	return Collections.EMPTY_LIST;
	    log.debug("RESOLVED GROUP: " + bestGroup.get().getObjectNames().toString());
	    if (relation == null) {
	    	Set<Type> types = bestGroup.get().getObjects().stream().map(o -> o.getType()).collect(Collectors.toSet());
	    	if (types.size() == 1) {
	    		Type type = types.iterator().next();
	    		SpatialRegion relation = config.DIR_EXCEPTIONS.get(type.getName());
	    		if (relation != null)
	    			this.relation = relation;
	    		else
	    			this.relation = config.DEFAULT_DIR;
	    	} else {
    			this.relation = config.DEFAULT_DIR;
	    	}
	    }
		List<IKBObject> objs = SpatialRegionComputer.computeObjectsForGroupRelation(bestGroup.get(), relation, ordinality, cardinality, config);
		if (objs == null)
			return Collections.EMPTY_LIST;
		scores = objs.stream().map(o -> new ObjectScore(o, 1)).toList();
		LogUtils.logScores(String.format("Totals after scoring GroupRel %s (ordinality=%s)", relation, ordinality), scores);
		return scores;
	}

	public Integer getOrdinality() {
		if (this.ordinality == null)
			return null;
		else
			return this.ordinality;
	}
}
