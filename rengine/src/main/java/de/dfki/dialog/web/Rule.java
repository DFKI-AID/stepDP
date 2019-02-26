package de.dfki.dialog.web;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class Rule {
    public String name = "unknown";
    public int priority = 0;
    public boolean active;
    public Set<String> tags = new HashSet<>();

    public Rule(String name, int priority) {
        this.name = name;
        this.priority = priority;
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
