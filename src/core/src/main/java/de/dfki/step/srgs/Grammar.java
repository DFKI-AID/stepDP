package de.dfki.step.srgs;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO add e.g. tag-format="semantics/1.0" to Grammar tag; is optional according to spec but MS ASR needs it
 * TODO add root to grammar, root_rule with refs; is optional according to spec but MS ASR needs it
 */
public class Grammar implements Node {
    private List<Rule> rules = new ArrayList<>();

    private String grammarType = "grammar"; //official=de.dfki.step.srgs ms=grammar

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public void write(NodeWriter nw) {
        writeBeginGrammar(nw);
        nw.increaseIndent();
        nw.newLine();
        rules.forEach(r -> {
            r.write(nw);
            nw.newLine();
        });
        nw.decreaseIndent();
        writeEndGrammar(nw);
    }

    protected void writeBeginGrammar(NodeWriter nw) {
        nw.newLine();
        nw.write(String.format("<!DOCTYPE %s PUBLIC \"-//W3C//DTD GRAMMAR 1.0//EN\" \"http://www.w3.org/TR/speech-%s/%s.dtd\">", grammarType, grammarType, grammarType));
        nw.newLine();
        nw.newLine();
        nw.write(String.format("<%s xmlns=\"http://www.w3.org/2001/06/%s\" xml:lang=\"en\"", grammarType, grammarType)).newLine();
        nw.write(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"").newLine();
        nw.write(String.format(" xsi:schemaLocation=\"http://www.w3.org/2001/06/%s", grammarType)).newLine();
        nw.write(String.format(" http://www.w3.org/TR/speech-%s/%s.xsd\"", grammarType, grammarType)).newLine();
        nw.write(" version=\"1.0\" mode=\"voice\" root=\"root_rule\" tag-format=\"semantics/1.0\">").newLine();
    }

    protected void writeEndGrammar(NodeWriter nw) {
        nw.newLine();
        nw.write(String.format("</%s>", grammarType)).newLine();
    }

    /**
     * The speech recognizer of Microsoft requires a root_rule.
     * This function generates this rule using all public rules.
     */
    protected void createRootRule() {
        Rule rootRule = new Rule("root_rule");
        OneOf oneOf = new OneOf();
        for(Rule rule : this.rules) {
            if(!rule.isPublic()){
                continue;
            }
            oneOf.add(new Item(new RuleRef(rule.getId())));
        }
        rootRule.add(oneOf);
        this.addRule(rootRule);
    }

    @Override
    public String toString() {
        NodeWriter nw = new NodeWriter();
        this.write(nw);
        return nw.getOutput();
    }
}
