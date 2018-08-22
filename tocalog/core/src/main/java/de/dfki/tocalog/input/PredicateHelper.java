package de.dfki.tocalog.input;

import de.dfki.tocalog.kb.EKnowledgeMap;
import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.model.Triple;

import java.util.Set;
import java.util.stream.Collectors;

/**
 */
public class PredicateHelper {
    protected KnowledgeBase kb;

    public PredicateHelper(KnowledgeBase kb) {
        this.kb = kb;
    }

    public Set<String> inverseRelation(EKnowledgeMap<Triple> predicateKm, String objectId) {
        Set<String> subjectIds;
        try {
            subjectIds = predicateKm.lock().getStream()
                    .map(e -> e.getValue())
                    .filter(t -> t.isSubjectPresent())
                    .filter(t -> t.isObjectPresent())
                    .filter(t -> t.getObject().get().equals(objectId))
                    .map(t -> t.getSubject().get())
                    .collect(Collectors.toSet());
        } finally {
            predicateKm.unlock();
        }

        return subjectIds;
    }

    public Set<String> relation(String subjectId, String predicate) {
        EKnowledgeMap<Triple> predicateKm = kb.getKnowledgeMap(Triple.class, predicate);
        return relation(subjectId, predicateKm);
    }


    public Set<String> relation(String subjectId, EKnowledgeMap<Triple> predicateKm) {
        Set<String> objectIds;
        try {
            objectIds = predicateKm.lock().getStream()
                    .map(e -> e.getValue())
                    .filter(t -> t.isSubjectPresent())
                    .filter(t -> t.isObjectPresent())
                    .filter(t -> t.getSubject().get().equals(subjectId))
                    .map(t -> t.getSubject().get())
                    .collect(Collectors.toSet());
        } finally {
            predicateKm.unlock();
        }

        return objectIds;
    }

    public Set<String> inverseRelation(String predicate, String objectId) {
        EKnowledgeMap<Triple> predicateKm = kb.getKnowledgeMap(Triple.class, predicate);
        return inverseRelation(predicateKm, objectId);
    }
}
