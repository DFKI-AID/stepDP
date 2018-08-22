package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.output.IMPP;
import de.dfki.tocalog.output.OutputComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class AllocationStateVisitor implements OutputNode.Visitor {
    private IMPP impp;
    private AllocationState allocationState;
    private Allocation allocation;

    public AllocationState visit(Allocation allocation) {
        this.impp = allocation.getImpp();
        this.allocation = allocation;
        allocation.getOutputNode().accept(this);
        return allocationState;
    }

    @Override
    public void visitLeaf(OutputNode.External leaf) {
        //TODO id not available
        String allocationId = allocation.getAllocationsIds().get(leaf.getId());
        for(OutputComponent oc : impp.getComponents()) {
//            oc.getState()
        }
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {

        List<AllocationState> allocationStates = new ArrayList<>();
        for (OutputNode outputNode : node.getChildNodes()) {
            outputNode.accept(this);
            allocationStates.add(allocationState);
        }

        allocationState = new AllocationState(AllocationState.State.NONE);
        switch (node.getSemantic()) {
            case redundant:
                allocationState = mergeRedundant(allocationStates);
                break;
            case complementary:
                throw new UnsupportedOperationException("not impl");
//                break;
            case alternative:
                throw new UnsupportedOperationException("not impl");
//                break;
            case concurrent:
                throw new UnsupportedOperationException("not impl");
//                break;
            case sequential:
                throw new UnsupportedOperationException("not impl");
//                break;
            case optional:
                throw new UnsupportedOperationException("not impl");
//                break;
        }

    }

    private AllocationState mergeRedundant(List<AllocationState> states) {
        if (states.stream().filter(s -> s.presenting()).findAny().isPresent()) {
            return new AllocationState(AllocationState.State.PRESENTING);
        }

        if (states.stream().filter(s -> s.cancelling()).findAny().isPresent()) {
            return new AllocationState(AllocationState.State.CANCEL);
        }


        if (states.stream().filter(s -> s.initializing()).findAny().isPresent()) {
            return new AllocationState(AllocationState.State.INIT);
        }

        if (states.stream().allMatch(s -> s.finished())) {
            boolean success = states.stream().filter(s -> s.successful()).findAny().isPresent();
            if (success) {
                // if one of the redundant presentation was successful the whole presentation is considered successful
                return new AllocationState(AllocationState.State.SUCCESS);
            }

            //TODO could merge error message of sub allocations
            return new AllocationState(states.get(0));
        }

        throw new IllegalStateException("unhandled presentation state: " +
                states.stream().map(as -> as.toString()).collect(Collectors.joining(" ")));
    }
}
