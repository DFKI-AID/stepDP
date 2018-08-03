package de.dfki.tocalog.kb;

import de.dfki.tractat.idl.Base;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 */
public class KnowledgeBase {
    private Map<Class, KnowledgeMap> kmap = new HashMap<>();
    private Map<Key, KnowledgeSet> kset = new HashMap<>();

    public <S extends Base> void initKnowledgeMap(Class<S> type, KnowledgeMap<S> ks) {
        if (kmap.containsKey(type)) {
            throw new IllegalArgumentException("KnowledgeBase already contains a KnowledgeStore for the type " + type);
        }
        kmap.put(type, ks);
    }

    public <S extends Base> KnowledgeMap<S> initKnowledgeMap(Class<S> type) {
        if (kmap.containsKey(type)) {
            //TODO print warning
            return kmap.get(type);
        }
        KnowledgeMap ks = new KnowledgeMap();
        kmap.put(type, ks);
        return ks;
    }


    public <S extends Base> Optional<KnowledgeMap<S>> getKnowledgeStore(Class<S> type) {
        KnowledgeMap ks = kmap.get(type);
        if (ks == null) {
            return Optional.empty();
        }
        return Optional.of(ks);
    }


    public <S extends Base> KnowledgeSet<S> initKnowledgeSet(Class<S> type, String id) {
        Key key = new Key(type, id);
        if (kset.containsKey(key)) {
            //TODO print warning
            return kset.get(type);
        }
        KnowledgeSet<S> k = new KnowledgeSet<S>(id);
        kset.put(key, k);
        return k;
    }


    public <S extends Base> Optional<KnowledgeSet<S>> getKnowledgeSet(Class<S> type, String id) {
        KnowledgeSet ks = kset.get(type);
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
