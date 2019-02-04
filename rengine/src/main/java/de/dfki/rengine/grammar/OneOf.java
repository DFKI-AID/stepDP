package de.dfki.rengine.grammar;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class OneOf implements Node {
    private List<Node> children = new ArrayList<>();

    public OneOf add(Node node) {
        this.children.add(node);
        return this;
    }

    @Override
    public void write(NodeWriter nw) {
        nw.write("<one-of>");
        nw.increaseIndent();
        nw.newLine();
        children.forEach(c -> {
            c.write(nw);
            nw.newLine();
        });
        nw.decreaseIndent();
        nw.write("</one-of");
    }
}
