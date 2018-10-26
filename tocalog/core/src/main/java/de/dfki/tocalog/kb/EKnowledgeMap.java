package de.dfki.tocalog.kb;

import de.dfki.tocalog.model.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 */
public class EKnowledgeMap<T extends Entity> extends KnowledgeMap<T> {
    private static Logger log = LoggerFactory.getLogger(EKnowledgeMap.class);
    private Random rdm = new Random();


    /**
     * e.g. store multiple alternative sentences in the map
     * @return A random entity from this map.
     */
    public Optional<T> getRandom() {
        try {
            Set<Map.Entry<String, T>> es = this.lock().getData().entrySet();
            if (es.isEmpty()) {
                return Optional.empty();
            }
            Iterator<Map.Entry<String, T>> iter = es.iterator();
            int index = rdm.nextInt(es.size());
            for (int i = 0; i < index - 1; i++) {
                iter.next();
            }
            return Optional.of(iter.next().getValue());
        } finally {
            this.unlock();
        }
    }

    public Optional<T> getNewest() {
        try {
            KnowledgeMap.Locked<T> l = this.lock();
            Optional<T> opte = l.getStream()
                    .map(e -> e.getValue())
                    .reduce((e1, e2) -> {
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


    public Collection<T> getFromSource(String source) {
        try {
            KnowledgeMap.Locked<T> l = this.lock();
            Set<T> entries = l.getStream()
                    .map(e -> e.getValue())
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
     * @param maxCount
     * @param comparator
     */
    public void limit(int maxCount, Comparator<? super T> comparator) {
        try {
            KnowledgeMap.Locked<T> l = this.lock();
            if (l.getData().size() < maxCount) {
                return;
            }

            List<T> ordered = l.getStream()
                    .map(e -> e.getValue())
                    .sorted(comparator).collect(Collectors.toList());
            maxCount = Math.min(maxCount, ordered.size());
            Iterator<T> iter = ordered.iterator();
            while (l.getData().size() > maxCount) {
                l.getData().remove(iter.next());
            }
        } finally {
            this.unlock();
        }
    }

    /**
     * @param maxCount
     */
    public void limitTimestamp(int maxCount) {
        limit(maxCount, Comparator.comparing(e -> e.getTimestamp().orElse(0l)));
    }

    //TODO other helper functions
}
