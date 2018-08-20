package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.model.Service;
import de.dfki.tocalog.output.IMPP;
import de.dfki.tocalog.output.OutputComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class AllocateVisitor implements OutputNode.Visitor {
    private static Logger log = LoggerFactory.getLogger(AllocateVisitor.class);
    private final IMPP immp;
    private Map<String, Assignment> assignments;
    private Map<String, String> allocationsIds;

    public AllocateVisitor(IMPP immp) {
        this.immp = immp;
    }

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        Assignment assignment = assignments.get(leaf.getId());
        assignment.limit(1);
        List<Service> services = assignment.getServices();
        if (services.isEmpty()) {
            log.warn("can't assign service to {}. no suitable service available", leaf.getOutput());
            return;
        }

        Service service = services.get(0);
        boolean assigned = false;
        for (OutputComponent oc : immp.getComponents()) {
            if (!oc.handles(leaf.getOutput(), service)) {
                continue;
            }

            String id = oc.allocate(leaf.getOutput(), service);
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

    public Map<String, String> visit(OutputNode node, Map<String, Assignment> assignments) {
        this.allocationsIds = new HashMap<>();
        this.assignments = assignments;
        node.accept(this);
        return allocationsIds;
    }
}
