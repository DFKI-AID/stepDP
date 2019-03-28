package de.dfki.step.output.imp;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.Ontology;
import de.dfki.step.output.Imp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OutputNode ->
 * Assigns all services that can present an output to the corresponding leaves
 */
public class FindCandidateVisitor implements OutputNode.Visitor {
    private final Imp imp;
    private DeviceSelector deviceSelector;
    private Map<String, OutputUnit> assignments;
    private Collection<Entity> services;

    public FindCandidateVisitor(Imp imp) {
        this.imp = imp;
        this.deviceSelector = new DeviceSelector(imp);
    }

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        Optional<OutputUnit> outputUnit = deviceSelector.process(leaf.getOutput());
        if (!outputUnit.isPresent()) {
            return;
        }

        assignments.put(leaf.getId(), outputUnit.get());
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {
        node.getChildNodes().forEach(n -> n.accept(this));
    }

    public Map<String, OutputUnit> visit(OutputNode outputNode) {
        this.assignments = new HashMap<>();
        this.services = imp.getKb().getKnowledgeMap(Ontology.Service).getAll();
        outputNode.accept(this);
        return assignments;
    }
}
