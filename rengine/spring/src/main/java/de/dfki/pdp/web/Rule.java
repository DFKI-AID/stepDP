package de.dfki.pdp.web;

import java.util.HashSet;
import java.util.Set;

/**
 * Data container for web api
 */
public class Rule {
    public String name = "unknown";
    public boolean active;
    public Set<String> tags = new HashSet<>();

    public Rule(String name) {
        this.name = name;
    }

    public Rule setActive(boolean active) {
        this.active = active;
        return this;
    }

    public Rule setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }
}
