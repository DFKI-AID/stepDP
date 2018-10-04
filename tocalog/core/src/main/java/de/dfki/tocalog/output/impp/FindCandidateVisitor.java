package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.model.Service;
import de.dfki.tocalog.output.Imp;
import de.dfki.tocalog.output.OutputComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Assigns all services that can present an output to the corresponding leaves
 */
public class FindCandidateVisitor implements OutputNode.Visitor {
    private final Imp immp;
    private Map<String, Assignment> assignments;
    private Set<Service> services;

    public FindCandidateVisitor(Imp immp) {
        this.immp = immp;
    }

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        if (!assignments.containsKey(leaf.getId())) {
            assignments.put(leaf.getId(), new Assignment(leaf.getOutput(), null));
        }

        for (OutputComponent oc : immp.getComponents()) {
            for (Service service : services) {
                if (!oc.handles(leaf.getOutput(), service)) {
                    continue;
                }
                assignments.get(leaf.getId()).addService(service);
            }
        }
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {
        node.getChildNodes().forEach(n -> n.accept(this));
    }

    public Map<String, Assignment> visit(OutputNode outputNode) {
        this.assignments = new HashMap<>();
        this.services = immp.getKb().getKnowledgeMap(Service.class).getAll();
        outputNode.accept(this);
        return assignments;
    }
}
