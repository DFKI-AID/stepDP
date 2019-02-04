package de.dfki.rengine.web;

/**
 */
public class Rule {
    public String name = "unknown";
    public int priority = 0;
    public boolean active;

    public Rule(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public Rule setActive(boolean active) {
        this.active = active;
        return this;
    }
}
