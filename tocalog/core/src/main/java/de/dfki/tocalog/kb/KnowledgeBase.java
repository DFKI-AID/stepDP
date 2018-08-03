package de.dfki.tocalog.kb;

import de.dfki.tocalog.model.Entity;
import de.dfki.tractat.idl.Base;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 */
public class KnowledgeBase {
    private Map<Class, EKnowledgeMap> kmap = new HashMap<>();
    private Map<Key, EKnowledgeSet> kset = new HashMap<>();

    public <S extends Entity> void initEKnowledgeMap(Class<S> type, EKnowledgeMap<S> ks) {
        if (kmap.containsKey(type)) {
            throw new IllegalArgumentException("KnowledgeBase already contains a KnowledgeStore for the type " + type);
        }
        kmap.put(type, ks);
    }

    public <S extends Entity> EKnowledgeMap<S> initEKnowledgeMap(Class<S> type) {
        if (kmap.containsKey(type)) {
            //TODO print warning
            return kmap.get(type);
        }
        EKnowledgeMap ks = new EKnowledgeMap();
        kmap.put(type, ks);
        return ks;
    }


    public <S extends Entity> Optional<EKnowledgeMap<S>> getKnowledgeStore(Class<S> type) {
        EKnowledgeMap ks = kmap.get(type);
        if (ks == null) {
            return Optional.empty();
        }
        return Optional.of(ks);
    }


    public <S extends Entity> EKnowledgeSet<S> initKnowledgeSet(Class<S> type, String id) {
        Key key = new Key(type, id);
        if (kset.containsKey(key)) {
            //TODO print warning
            return kset.get(type);
        }
        EKnowledgeSet<S> k = new EKnowledgeSet<S>(id);
        kset.put(key, k);
        return k;
    }


    public <S extends Entity> Optional<EKnowledgeSet<S>> getEKnowledgeSet(Class<S> type, String id) {
        EKnowledgeSet ks = kset.get(type);
        if (ks == null) {
            return Optional.empty();
        }
        return Optional.of(ks);
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
