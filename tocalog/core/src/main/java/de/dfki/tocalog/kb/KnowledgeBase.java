package de.dfki.tocalog.kb;

import de.dfki.tocalog.model.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 */
public class KnowledgeBase {
    private Map<Class, EKnowledgeMap> kmap = new HashMap<>();
    private Map<Key, EKnowledgeSet> kset = new HashMap<>();

    public synchronized <S extends Entity> EKnowledgeMap<S> getKnowledgeStore(Class<S> type) {
        EKnowledgeMap ks = kmap.get(type);
        if (ks == null) {
            ks = new EKnowledgeMap();
            kmap.put(type, ks);
        }
        return ks;
    }

    public synchronized <S extends Entity> EKnowledgeSet<S> getEKnowledgeSet(Class<S> type, String id) {
        Key key = new Key(type, id);
        EKnowledgeSet ks = kset.get(type);
        if (ks == null) {
            ks = new EKnowledgeSet<S>(id);
            kset.put(key, ks);
        }
        return ks;
    }


    private class Key {
        private Class clazz;
        private String id;

        public Key(Class clazz, String id) {
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
