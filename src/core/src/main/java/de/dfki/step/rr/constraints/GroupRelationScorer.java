package de.dfki.step.rr.constraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.kb.SpatialRegion;
import de.dfki.step.rr.ObjectGroup;
import de.dfki.step.util.LogUtils;

public class GroupRelationScorer extends RelationScorer {
    private static final Logger log = LoggerFactory.getLogger(GroupRelationScorer.class);
	private static final int DEFAULT_PRIO = 6000;
	private SpatialRegion relation;
	private Integer ordinality;

	public GroupRelationScorer(IKBObject constraint, KnowledgeBase kb) {
		super(constraint, kb);
		// TODO: make conversion from string to rel more flexible (e.g. case insensitive etc.)
		this.relation = SpatialRegion.valueOf(constraint.getString("relation"));
		this.ordinality = constraint.getInteger("ordinality");
		this.setPriority(DEFAULT_PRIO);
	}

	@Override
	public List<ObjectScore> updateScores(List<ObjectScore> scores) {
		List<ObjectGroup> potentialGroups = ObjectGroup.findGroupCandidates(scores, ordinality, -1);
		if (potentialGroups == null || potentialGroups.isEmpty())
			return Collections.EMPTY_LIST;
		// TODO: consider also other potential groups
		Optional<ObjectGroup> bestGroup = potentialGroups
			      				.stream()
			      				.min(Comparator.comparing(ObjectGroup::getGroupConfidence));
	    if (!bestGroup.isPresent())
	    	return Collections.EMPTY_LIST;
	    log.debug("RESOLVED GROUP: " + bestGroup.get().getObjectNames().toString());
		IKBObject obj = SpatialRegionComputer.computeObjectForGroupRelation(bestGroup.get(), relation, ordinality);
		if (obj == null)
			return Collections.EMPTY_LIST;
		scores = List.of(new ObjectScore(obj, 1));
		LogUtils.logScores(String.format("Totals after scoring GroupRel %s (ordinality=%s)", relation, ordinality), scores);
		return scores;
	}

}
