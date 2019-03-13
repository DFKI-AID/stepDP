package de.dfki.step.srgs;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TagBuilder {
    private Map<String, Object> tagContent = new HashMap<>();

    public String build() {
        var gson = new Gson();
        var tag = gson.toJson(tagContent);
        return tag;
    }

    public TagBuilder intent(String intent) {
        tagContent.put("intent", intent);
        return this;
    }

    public TagBuilder field(String name, String value) {
        tagContent.put(name, value);
        return this;
    }

    public static TagBuilder builder() {
        return new TagBuilder();
    }
}
