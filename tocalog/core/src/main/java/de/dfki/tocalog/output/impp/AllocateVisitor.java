package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.model.Service;
import de.dfki.tocalog.output.Imp;
import de.dfki.tocalog.output.OutputComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Uses the first service of a node and allocates the output for it.
 * The result is a map that contains the allocation ids for each node.
 */
public class AllocateVisitor implements OutputNode.Visitor {
    private static Logger log = LoggerFactory.getLogger(AllocateVisitor.class);
    private final Imp impp;
    private Map<String, Assignment> assignments;
    private Map<String, String> allocationsIds;

    public AllocateVisitor(Imp impp) {
        this.impp = impp;
    }

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        Assignment assignment = assignments.get(leaf.getId());
        assignment.limit(1);

        Optional<Service> service = assignment.getBest();
//        List<Service> services = assignment.getServices();
        if (!service.isPresent()) {
            log.warn("can't assign service to {}. no suitable service available", leaf.getOutput());
            return;
        }

        //TODO case: service is lost and output component can't assign anymore
        // could try again with the second best?
        // or fail here and try reschedule on higher level? <- easier to reason

        boolean assigned = false;
        for (OutputComponent oc : impp.getComponents()) {
            if (!oc.handles(leaf.getOutput(), service.get())) {
                continue;
            }

            String id = oc.allocate(leaf.getOutput(), service.get());
            allocationsIds.put(leaf.getId(), id);
            assigned = true;
            break;
        }

        if (!assigned) {
            log.warn("can't assign service to {}. no output component can handle the request", leaf.getOutput());
        }
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {
        node.getChildNodes().forEach(n -> n.accept(this));
        //TODO store allocation state object with the semantic of the inner node
    }

    public Allocation visit(OutputNode node, Map<String, Assignment> assignments) {
        this.allocationsIds = new HashMap<>();
        this.assignments = assignments;
        node.accept(this);
        return new Allocation(impp, node, allocationsIds);
    }
}
