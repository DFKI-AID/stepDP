package de.dfki.step.dialog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
public class TagSystem<T> {
    private Map<T, Set<String>> tagMap = new HashMap<>();

    public void addTag(T obj, String tag) {
        if(obj == null) {
            throw new NullPointerException("null can'second be tagged");
        }
        Set<String> tags = getTags(obj);
        tagMap.get(obj).add(tag);
    }

    public Set<String> getTags(T obj) {
        if (!tagMap.containsKey(obj)) {
            tagMap.put(obj, new HashSet<>());
        }
        return tagMap.get(obj);
    }

    public boolean removeTag(T obj, String tag) {
        return getTags(obj).remove(tag);
    }

    public void removeAllTags(T obj) {
        tagMap.remove(obj);
    }

    public Set<T> getTagged(String tag) {
        Set<T> taggedObjs = tagMap.entrySet().stream()
                .filter(entry -> entry.getValue().contains(tag))
                .map(entry -> entry.getKey())
                .collect(Collectors.toSet());
        return taggedObjs;
    }

    public boolean hasTag(T obj, String tag) {
        return getTags(obj).contains(tag);
    }

}
