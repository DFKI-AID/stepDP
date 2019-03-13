package de.dfki.step.srgs;

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
        children.forEach(c -> {
            nw.newLine();
            c.write(nw);
        });
        nw.decreaseIndent();
        nw.newLine();
        nw.write("</one-of>");
    }
}
