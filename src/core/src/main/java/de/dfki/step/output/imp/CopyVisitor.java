package de.dfki.step.output.imp;

import java.util.Stack;

/**
 */
public class CopyVisitor implements OutputNode.Visitor {
    private Stack<OutputNode.Internal.Builder> nodes = new Stack<>();
    private OutputNode.Internal.Builder copy;

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        nodes.peek().addNode(leaf.copy());
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {

        OutputNode.Internal.Builder copyBuilder = OutputNode.build(node.getSemantic());
        copyBuilder.setId(node.getId());
        nodes.push(copyBuilder);
        for(OutputNode child : node.getChildNodes()) {
            child.accept(this);
        }
        nodes.pop();
        copy = copyBuilder;
        if(!nodes.isEmpty()) {
            nodes.peek().addNode(copy.build());
        }

    }

    public OutputNode.Internal.Builder copy(OutputNode node) {
        nodes.clear();
        node.accept(this);
        return copy;
    }
}
