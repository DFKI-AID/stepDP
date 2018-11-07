package de.dfki.tocalog.kb;


import java.util.HashMap;
import java.util.Map;

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

//    public synchronized KMView getView(InheritanceTree tree, Type type) {
//
//    }
}
