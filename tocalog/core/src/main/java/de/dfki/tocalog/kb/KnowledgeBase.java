package de.dfki.tocalog.kb;

import de.dfki.tocalog.model.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 */
public class KnowledgeBase {
    private static final String DEFAULT_ID = "default";
    private Map<Key, EKnowledgeMap> kmap = new HashMap<>();
    private Map<Key, EKnowledgeSet> kset = new HashMap<>();

    public synchronized <S extends Entity> EKnowledgeMap<S> getKnowledgeMap(Class<S> type) {
        Key key = getKey(type);
        return getKnowledgeMap(key);
    }

    public synchronized <S extends Entity> EKnowledgeMap<S> getKnowledgeMap(Key<S> key) {
        EKnowledgeMap ks = kmap.get(key);
        if (ks == null) {
            ks = new EKnowledgeMap();
            kmap.put(key, ks);
        }
        return ks;
    }

    public synchronized <S extends Entity> EKnowledgeMap<S> getKnowledgeMap(Class<S> type, String id) {
        Key key = getKey(type, id);
        return getKnowledgeMap(key);
    }

    public synchronized <S extends Entity> EKnowledgeSet<S> getKnowledgeSet(Class<S> type, String id) {
        Key key = getKey(type, id);
        EKnowledgeSet ks = kset.get(type);
        if (ks == null) {
            ks = new EKnowledgeSet<S>(id);
            kset.put(key, ks);
        }
        return ks;
    }

    public static <S extends Entity> Key<S> getKey(Class<S> type) {
        return new Key(type, DEFAULT_ID);
    }

    public static <S extends Entity> Key<S> getKey(Class<S> type, String id) {
        return new Key(type, id);
    }

    public static class Key<T> {
        private Class<T> clazz;
        private String id;

        public Key(Class<T> clazz, String id) {
            this.clazz = clazz;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(clazz, key.clazz) &&
                    Objects.equals(id, key.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, id);
        }
    }
}
