package de.dfki.tocalog.kb;


import java.util.HashMap;
import java.util.Map;

/**
 */
public class KnowledgeBase {
    private static final String DEFAULT_ID = "default";
    private Map<String, KnowledgeMap> kmap = new HashMap<>();
//    private Map<String, KnowledgeSet> kset = new HashMap<>();

    public synchronized KnowledgeMap getKnowledgeMap(String id) {
        KnowledgeMap ks = kmap.get(id);
        if (ks == null) {
            ks = new KnowledgeMap();
            kmap.put(id, ks);
        }
        return ks;
    }
}
