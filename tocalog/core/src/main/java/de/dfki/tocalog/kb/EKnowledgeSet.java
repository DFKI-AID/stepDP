package de.dfki.tocalog.kb;

import de.dfki.tocalog.model.Entity;

import java.util.*;
import java.util.stream.Collectors;

/**
 */
public class EKnowledgeSet<T extends Entity> extends KnowledgeSet<T> {
    public EKnowledgeSet(String id) {
        super(id);
    }

    public Optional<T> getNewest() {
        try {
            KnowledgeSet.Locked<T> l = this.lock();
            Optional<T> opte = l.getStream().reduce((e1, e2) -> {
                if (e1.getTimestamp().orElse(0l) < e2.getTimestamp().orElse(0l)) {
                    return e2;
                }
                return e1;
            });
            opte = opte.map(e -> l.copy(e));
            return opte;
        } finally {
            this.unlock();
        }
    }


    public void removeOld(long timeout) {
        try {
            long now = System.currentTimeMillis();
            KnowledgeSet.Locked<T> l = this.lock();
            Set<T> old = l.getStream().filter(e -> e.getTimestamp().orElse(0l) + timeout < now).collect(Collectors.toSet());
            l.removeAll(old);
        } finally {
            this.unlock();
        }
    }

    public Collection<T> getFromSource(String source) {
        try {
            KnowledgeSet.Locked<T> l = this.lock();
            Set<T> entries = l.getStream()
                    .filter(e -> e.getSource().orElse("").equals(source))
                    .map(e -> l.copy(e))
                    .collect(Collectors.toSet());
            return entries;
        } finally {
            this.unlock();
        }
    }

    public static <T extends Entity> void removeAllFromSource(KnowledgeSet<T> kset, String source) {
        kset.removeIf(e -> e.getSource().orElse("").equals(source));
    }

    /**
     * Limits the number of entries inside the set: This method will remove entries such that the total count of
     * entries is less than the given size.
     *
     * @param max
     * @param comparator
     */
    public void limit(int max, Comparator<? super T> comparator) {
        try {
            KnowledgeSet.Locked<T> l = this.lock();
            if (l.getData().size() < max) {
                return;
            }

            List<T> ordered = l.getData().stream().sorted(comparator).collect(Collectors.toList());
            max = Math.min(max, ordered.size());
            Iterator<T> iter = ordered.iterator();
            while (l.getData().size() > max) {
                l.getData().remove(iter.next());
            }
        } finally {
            this.unlock();
        }
    }

    /**
     *
     * @param max
     */
    public void limitTimestamp(int max) {
        limit(max, Comparator.comparing(e -> e.getTimestamp().orElse(0l)));
    }
}