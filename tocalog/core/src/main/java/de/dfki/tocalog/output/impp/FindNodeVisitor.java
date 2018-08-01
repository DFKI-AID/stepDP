package de.dfki.tocalog.output.impp;

import java.util.Optional;
import java.util.function.Predicate;

/**
 */
public class FindNodeVisitor implements OutputNode.Visitor {
    private Predicate<OutputNode> filter;
    private OutputNode result;

    public FindNodeVisitor(Predicate<OutputNode> filter) {
        this.filter = filter;
    }

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        if (filter.test(leaf)) {
            result = leaf;
            return;
        }
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {
        if (filter.test(node)) {
            result = node;
            return;
        }

        for (OutputNode child : node.getChildNodes()) {
            child.accept(this);
            if (result != null) {
                return;
            }
        }
    }

    public Optional<OutputNode> find(OutputNode node) {
        this.result = null;
        node.accept(this);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(result);
    }
}
