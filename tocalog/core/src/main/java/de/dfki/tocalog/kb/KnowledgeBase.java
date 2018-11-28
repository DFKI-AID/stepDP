package de.dfki.tocalog.kb;


import java.util.*;

/**
 */
public class KnowledgeBase {
    private static final String DEFAULT_ID = "default";
    private Map<String, KnowledgeMap> kmaps = new HashMap<>();
    private Map<String, KnowledgeList> klists = new HashMap<>();
//    private Map<String, KnowledgeSet> kset = new HashMap<>();

    public synchronized KnowledgeMap getKnowledgeMap(Type type) {
        return getKnowledgeMap(type.getName());
    }

    public synchronized KnowledgeMap getKnowledgeMap(String id) {
        KnowledgeMap ks = kmaps.get(id);
        if (ks == null) {
            ks = new KnowledgeMap();
            kmaps.put(id, ks);
        }
        return ks;
    }

    public synchronized KnowledgeList getKnowledgeList(Type type) {
        return getKnowledgeList(type.getName());
    }

    public synchronized KnowledgeList getKnowledgeList(String id) {
        KnowledgeList kl = klists.get(id);
        if (kl == null) {
            kl = new KnowledgeList();
            klists.put(id, kl);
        }
        return kl;
    }

    /**
     * @param typeHierarchy
     * @param type
     * @return A view on the given type and all its subclasses
     */
    public synchronized KMView getView(TypeHierarchy typeHierarchy, Type type) {
        Set<Type> types = typeHierarchy.getSubClasses(type);
        types.add(type);
        return getView(types);
    }

    /**
     * @param types
     * @return A view on the given types only
     */
    public synchronized KMView getView(Collection<Type> types) {
        Set<KnowledgeMap> kmaps = new HashSet<>();
        for (Type type : types) {
            kmaps.add(getKnowledgeMap(type));
        }
        return new KMView(kmaps);
    }
}
