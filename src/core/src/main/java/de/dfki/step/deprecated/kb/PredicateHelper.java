package de.dfki.step.deprecated.kb;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
@Deprecated
public class PredicateHelper {
    protected KnowledgeBase kb;

    public PredicateHelper(KnowledgeBase kb) {
        this.kb = kb;
    }

//    public Set<String> inverseRelation(EKnowledgeMap<Triple> predicateKm, String objectId) {
//        Set<String> subjectIds;
//        try {
//            subjectIds = predicateKm.lock().getStream()
//                    .map(e -> e.getValue())
//                    .filter(t -> t.isSubjectPresent())
//                    .filter(t -> t.isObjectPresent())
//                    .filter(t -> t.getObject().get().equals(objectId))
//                    .map(t -> t.getSubject().get())
//                    .collect(Collectors.toSet());
//        } finally {
//            predicateKm.unlock();
//        }
//
//        return subjectIds;
//    }

    public Set<String> relation(String subjectId, KnowledgeMap predicateKm) {
        Collection<Entity> objects = predicateKm.query(e -> e.get(Relation.subject).orElse("").equals(subjectId));
        Set<String> objectIds = objects.stream()
                    .map(e -> e.get(Relation.subject).get())
                    .collect(Collectors.toSet());

        return objectIds;
    }
}
