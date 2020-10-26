package de.dfki.step.srgs;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OneOf implements Node {
    private List<Node> children = new ArrayList<>();

    public OneOf add(Item item) {
        this.children.add(item);
        return this;
    }

    public OneOf add(RuleRef ruleRef) {
        //rule ref has to be below item.
        Node node  = nw -> {
            nw.write("<item>");
            ruleRef.write(nw);
            nw.write("</item>");
        };
        children.add(node);
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
