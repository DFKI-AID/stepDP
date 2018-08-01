package de.dfki.tocalog.output.impp;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class GetAssignedServiceVisitor implements OutputNode.Visitor {
    private Set<String> services = new HashSet();

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        services.addAll(leaf.getServices());
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {
        for(OutputNode child : node.getChildNodes()) {
            child.accept(this);
        }
    }

    public Set<String> getAssignedServices(OutputNode node) {
        services = new HashSet<>();
        node.accept(this);
        return services;
    }
}
