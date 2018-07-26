package de.dfki.tocalog.core.kb;

import de.dfki.tractat.idl.Base;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 */
public class KnowledgeBase {
    private Map<Class, KnowledgeStore> kss = new HashMap<>();

    public <S extends Base<S>> void initKnowledgeStore(Class<S> type, KnowledgeStore<S> ks) {
        if(kss.containsKey(type)) {
            throw new IllegalArgumentException("KnowledgeBase already contains a KnowledgeStore for the type " + type);
        }
        kss.put(type, ks);
    }

    public <S extends Base<S>> KnowledgeStore<S> initKnowledgeStore(Class<S> type) {
        if(kss.containsKey(type)) {
//            throw new IllegalArgumentException("KnowledgeBase already contains a KnowledgeStore for the type " + type);
            return kss.get(type);
        }
        KnowledgeStore ks = new KnowledgeStore();
        kss.put(type, ks);
        return ks;
    }

    public <S extends Base<S>> Optional<KnowledgeStore<S>> getKnowledgeStore(Class<S> type) {
        KnowledgeStore ks = kss.get(type);
        if(ks == null) {
            return Optional.empty();
        }
        return Optional.of(ks);
    }
}
