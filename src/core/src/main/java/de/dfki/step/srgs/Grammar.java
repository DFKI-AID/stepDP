package de.dfki.step.srgs;

import java.util.ArrayList;
import java.util.List;

/**
 *
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
        nw.write("<!DOCTYPE srgs PUBLIC \"-//W3C//DTD GRAMMAR 1.0//EN\" \"http://www.w3.org/TR/speech-srgs/srgs.dtd\">");
        nw.newLine();
        nw.newLine();
        nw.write("<srgs xmlns=\"http://www.w3.org/2001/06/srgs\" xml:lang=\"en\"").newLine();
        nw.write(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"").newLine();
        nw.write(" xsi:schemaLocation=\"http://www.w3.org/2001/06/srgs").newLine();
        nw.write(" http://www.w3.org/TR/speech-srgs/srgs.xsd\"").newLine();
        nw.write(" version=\"1.0\" mode=\"voice\" root=\"basicCmd\">").newLine();
    }

    protected void writeEndGrammar(NodeWriter nw) {
        nw.newLine();
        nw.write("</srgs>").newLine();
    }

    @Override
    public String toString() {
        NodeWriter nw = new NodeWriter();
        this.write(nw);
        return nw.getOutput();
    }
}