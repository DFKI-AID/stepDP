package de.dfki.step.output.imp;

import de.dfki.step.kb.Entity;
import de.dfki.step.output.Imp;
import de.dfki.step.output.OutputComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Uses the first service of a node and allocates the output for it.
 * The result is a map that contains the allocation ids for each node.
 */
public class AllocateVisitor implements OutputNode.Visitor {
    private static Logger log = LoggerFactory.getLogger(AllocateVisitor.class);
    private final Imp imp;
    private Map<String, OutputUnit> assignments;
    private Map<String, String> allocationsIds;

    public AllocateVisitor(Imp imp) {
        this.imp = imp;
    }

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        String id = leaf.getId();
        OutputUnit outputUnit = assignments.get(id);

//        assignment.limit(1);

        if(outputUnit == null) {
            log.warn("can't allocate {}: no service assigned.", outputUnit);
            return;
        }

        Set<Entity> services = assignments.get(id).getServices();
//        List<Service> services = assignment.getServices();
        if (services.isEmpty()) {
            log.warn("can't allocate {}: no suitable service available.", outputUnit);
            return;
        }



        //TODO case: service is lost and output component can't assign anymore
        // could try again with the second best?
        // or fail here and try reschedule on higher level? <- easier to reason

        boolean assigned = false;
        for (OutputComponent oc : imp.getComponents()) {
            if (!oc.supports(outputUnit.getOutput(), services.iterator().next())) {
                continue;
            }
            String allocateId = oc.allocate(outputUnit); //TODO all on same comp

//            String id = oc.allocate(leaf.getAttachment(), service.get());
            allocationsIds.put(leaf.getId(), allocateId);
            assigned = true;
            break;
        }

        if (!assigned) {
            log.warn("can't allocate {}: no output component can handle the request", outputUnit);
        }
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {
        node.getChildNodes().forEach(n -> n.accept(this));
        //TODO store allocation state object with the semantic of the inner node.. not necessary?
    }

    public Allocation visit(OutputNode node, Map<String, OutputUnit> assignments) {
        this.allocationsIds = new HashMap<>();
        this.assignments = assignments;
        node.accept(this);
        return new Allocation(imp, node, allocationsIds);
    }
}
