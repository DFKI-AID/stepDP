package de.dfki.tocalog.kb;

import de.dfki.tocalog.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

/**
 */
public class LocatedInProperty extends PredicateHelper {
    private static Logger log = LoggerFactory.getLogger(LocatedInProperty.class);
    private static final String LOCATED_IN_PRED = "located_in";
    private EKnowledgeMap<Zone> zoneKm;

    public LocatedInProperty(KnowledgeBase kb) {
        super(kb);
        zoneKm = kb.getKnowledgeMap(Zone.class);
    }

    public Set<Zone> locatedIn(String id) {
        Set<String> objectIds = relation(id, LOCATED_IN_PRED);
        return zoneKm.get(objectIds);
    }

    public Set<String> locates(Zone zone) {
        if (!zone.getId().isPresent()) {
            log.warn("can't  'locates' with a zone without an id. got {}", zone);
            return Collections.EMPTY_SET;
        }
        return inverseRelation(LOCATED_IN_PRED, zone.getId().get());
    }

}
