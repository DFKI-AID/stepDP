package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.kb.KnowledgeMap;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * assigns 'console output' to each leaf
 */
public class ConsoleAssigner implements OutputNode.Visitor {
    private static final String type = "console";
    private KnowledgeMap km;
    private Entity service;

    /**
     * @param km KnowledgeMap that contains services
     */
    public ConsoleAssigner(KnowledgeMap km) {
        this.km = km;
    }

    @Override
    public void visitLeaf(OutputNode.External leaf) {
//        if(leaf.getServices().isEmpty()) {
//            leaf.addService(service.get(Ontology.id).get()); //TODO get() -> use scheme
//        }
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {
        for(OutputNode child : node.getChildNodes()) {
            child.accept(this);
        }
    }

    public void assignConsoleService(OutputNode node) {
        //TODO filter here for services with valid id and type
        Set<Entity> services = km.getStore().values().stream()
                .filter(s -> s.get(Ontology.type).orElse("").equals(type))
                .collect(Collectors.toSet());
        if(services.isEmpty()) {
            return;
        }
        service = services.iterator().next();
        node.accept(this);
        service = null;
    }
}
