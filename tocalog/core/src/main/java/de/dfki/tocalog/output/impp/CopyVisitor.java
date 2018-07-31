package de.dfki.tocalog.output.impp;

import java.util.Stack;

/**
 */
public class CopyVisitor implements OutputNode.Visitor {
    private Stack<OutputNode.InnerNode.Builder> nodes = new Stack<>();
    private OutputNode copy;

    @Override
    public void visitLeaf(OutputNode.Leaf leaf) {
        nodes.peek().addNode(leaf.copy());
    }

    @Override
    public void visitInnerNode(OutputNode.InnerNode node) {

        OutputNode.InnerNode.Builder copyBuilder = OutputNode.buildNode(node.getSemantic());
        node.getId().ifPresent(id -> copyBuilder.setId(id));
        nodes.push(copyBuilder);
        for(OutputNode child : node.getChildNodes()) {
            child.accept(this);
        }
        nodes.pop();
        copy = copyBuilder.build();
        if(!nodes.isEmpty()) {
            nodes.peek().addNode(copy);
        }
    }

    public OutputNode getCopy(OutputNode node) {
        nodes.clear();
        node.accept(this);
        return copy;
    }
}
