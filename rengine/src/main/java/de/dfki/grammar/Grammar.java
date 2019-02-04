package de.dfki.grammar;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class Grammar implements Node {
    private List<Rule> rules = new ArrayList<>();

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public void write(NodeWriter nw) {
        //TODO header
        nw.increaseIndent();
        nw.newLine();
        rules.forEach(r -> {
            r.write(nw);
            nw.newLine();
        });
        nw.decreaseIndent();
    }
}
