package de.dfki.tocalog.output.impp;

import java.util.Stack;

/**
 */
public class CopyVisitor implements OutputNode.Visitor {
    private Stack<OutputNode.InnerNode.Builder> nodes = new Stack<>();
    private OutputNode.InnerNode.Builder copy;

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
        if(!nodes.isEmpty()) {
            nodes.peek().addNode(copy.build());
        }
        copy = copyBuilder;

    }

    public OutputNode.InnerNode.Builder copy(OutputNode node) {
        nodes.clear();
        node.accept(this);
        return copy;
    }
}
