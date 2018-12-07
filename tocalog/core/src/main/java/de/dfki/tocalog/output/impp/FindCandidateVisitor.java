package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.output.Imp;
import de.dfki.tocalog.output.OutputComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * OutputNode ->
 * Assigns all services that can present an output to the corresponding leaves
 */
public class FindCandidateVisitor implements OutputNode.Visitor {
    private final Imp immp;
    private Map<String, Assignment> assignments;
    private Collection<Entity> services;

    public FindCandidateVisitor(Imp immp) {
        this.immp = immp;
    }

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        if (!assignments.containsKey(leaf.getId())) {
            assignments.put(leaf.getId(), new Assignment(leaf.getAttachment(), null));
        }

        for (OutputComponent oc : immp.getComponents()) {
            for (Entity service : services) {
                if (!oc.supports(leaf.getAttachment(), service)) {
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
        this.services = immp.getKb().getKnowledgeMap(Ontology.Service).getAll();
        outputNode.accept(this);
        return assignments;
    }
}
