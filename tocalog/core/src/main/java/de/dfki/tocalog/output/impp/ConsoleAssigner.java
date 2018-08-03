package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.model.Service;

import java.util.Set;

/**
 * assigns 'console output' to each leaf
 */
public class ConsoleAssigner implements OutputNode.Visitor {
    private static final String type = "console";
    private KnowledgeMap<Service> serviceKB;
    private Service service;

    public ConsoleAssigner(KnowledgeMap<Service> services) {
        this.serviceKB = services;
    }

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        if(leaf.getServices().isEmpty()) {
            leaf.addService(service.getId().get()); //TODO get()
        }
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {
        for(OutputNode child : node.getChildNodes()) {
            child.accept(this);
        }
    }

    public void assignConsoleService(OutputNode node) {
        //TODO filter here for services with valid id and type
        Set<Service> services = serviceKB.getIf(s -> s.getType().orElse("").equals(type));
        if(services.isEmpty()) {
            return;
        }
        service = services.iterator().next();
        node.accept(this);
        service = null;
    }
}
