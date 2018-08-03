package de.dfki.tocalog.kb;

import de.dfki.tocalog.model.Entity;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
public class KBHelper {
    public static <T extends Entity> Optional<T> getNewest(KnowledgeSet<T> kset) {
        try {
            KnowledgeSet.Locked<T> l = kset.lock();
            Optional<T> opte = l.getStream().reduce((e1, e2) -> {
                if (e1.getTimestamp().orElse(0l) < e2.getTimestamp().orElse(0l)) {
                    return e2;
                }
                return e1;
            });
            opte = opte.map(e -> l.copy(e));
            return opte;
        } finally {
            kset.unlock();
        }
    }


    public static <T extends Entity> void removeOld(KnowledgeSet<T> kset, long timeout) {
        try {
            long now = System.currentTimeMillis();
            KnowledgeSet.Locked<T> l = kset.lock();
            Set<T> old = l.getStream().filter(e -> e.getTimestamp().orElse(0l) + timeout < now).collect(Collectors.toSet());
            l.removeAll(old);
        } finally {
            kset.unlock();
        }
    }

    public static <T extends Entity> Collection<T> getFromSource(KnowledgeSet<T> kset, String source) {
        try {
            KnowledgeSet.Locked<T> l = kset.lock();
            Set<T> entries = l.getStream()
                    .filter(e -> e.getSource().orElse("").equals(source))
                    .map(e -> l.copy(e))
                    .collect(Collectors.toSet());
            return entries;
        } finally {
            kset.unlock();
        }
    }

    public static <T extends Entity> void removeAllFromSource(KnowledgeSet<T> kset, String source) {
        kset.removeIf(e -> e.getSource().orElse("").equals(source));
    }

}
