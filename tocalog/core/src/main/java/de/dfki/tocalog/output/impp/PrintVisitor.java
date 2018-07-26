package de.dfki.tocalog.output.impp;

import org.apache.commons.lang3.StringUtils;

/**
 */
public class PrintVisitor implements OutputNode.Visitor {
    private StringBuilder sb;
    private int indentLevel;

    public void visitLeaf(OutputNode.Leaf leaf) {
        addIndentation();
        sb.append(leaf.getOutput());
        String servicesStr = leaf.getServices().stream().reduce("", (s1, s2) -> s1 + " | " + s2) + " | ";
        sb.append(" on ").append(servicesStr);
        sb.append("\n");
    }

    @Override
    public void visitInnerNode(OutputNode.InnerNode node) {
        addIndentation();
        sb.append(node.getSemantic());
        sb.append("\n");
        indentLevel += 4;
        for (OutputNode child : node.getChildNodes()) {
            child.accept(this);
        }
        indentLevel -= 4;
    }

    protected void addIndentation() {
        sb.append(StringUtils.repeat(" ", indentLevel));
    }

    public String print(OutputNode node) {
        sb = new StringBuilder();
        indentLevel = 0;
        node.accept(this);
        if (sb == null) {
            return "Empty Output Node";
        }
        return sb.toString();
    }
}
