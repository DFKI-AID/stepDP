package de.dfki.step.srgs;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages multiple sub-grammars and of a SRGS (xml) representation.
 * TODO: add function to add de.dfki.step.srgs string
 */
public class GrammarManager {
    private Map<String, Rule> rules = new HashMap<>();
    private Map<String, Boolean> activeMap = new HashMap<>();


    public synchronized boolean isActive(String id) {
        if(!activeMap.containsKey(id)) {
            return false;
        }
        return activeMap.get(id);
    }

    public synchronized void deactivateAll() {
        activeMap.keySet().forEach(id -> activeMap.put(id, false));
    }

    public synchronized void addRule(Rule rule) {
        this.rules.put(rule.getId(), rule);
    }

    public synchronized void setActive(String id, boolean active) {
        this.activeMap.put(id, active);
    }

    public synchronized Grammar createGrammar() {
        //TODO at the moment no rule is filtered.
        Grammar grammar = new Grammar();
        rules.entrySet().stream()
                //find all functions that are either active or not public
//                .filter(entry -> isActive(entry.getKey()) || !entry.getValue().isPublic())
                .map(entry -> entry.getValue())
                .forEach(rule -> grammar.addRule(rule));
        grammar.createRootRule();
        return grammar;
    }



}
