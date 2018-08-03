package de.dfki.tocalog.output.impp;

import org.apache.commons.lang3.StringUtils;

/**
 */
public class PrintVisitor implements OutputNode.Visitor {
    private StringBuilder sb;
    private int indentLevel;

    public void visitLeaf(OutputNode.External leaf) {
        addIndentation();
        sb.append(leaf.getOutput());
        String servicesStr = leaf.getServices().stream().reduce("", (s1, s2) -> s1 + " | " + s2) + " | ";
        sb.append(" on ").append(servicesStr);
        sb.append("\n");
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {
        addIndentation();
        sb.append(node.getSemantic());
        node.getId().ifPresent(id -> sb.append(" <" + id + ">"));
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

    public synchronized String printNode(OutputNode node) {
        sb = new StringBuilder();
        indentLevel = 0;
        node.accept(this);
        if (sb == null) {
            return "Empty Output Node";
        }
        return sb.toString();
    }

    public static PrintVisitor instance = new PrintVisitor();

    public static String print(OutputNode node) {
        return instance.printNode(node);
    }
}
