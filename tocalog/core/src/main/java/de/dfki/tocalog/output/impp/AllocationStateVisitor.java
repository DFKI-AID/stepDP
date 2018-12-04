package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.output.Imp;
import de.dfki.tocalog.output.OutputComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * output tree => allocation state
 */
public class AllocationStateVisitor implements OutputNode.Visitor {
    private static Logger log = LoggerFactory.getLogger(AllocationStateVisitor.class);
    private Imp impp;
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
        allocationState = AllocationState.getNone();
        String allocationId = allocation.getAllocationsIds().get(leaf.getId());
        for (OutputComponent oc : impp.getComponents()) {
            allocationState = oc.getAllocationState(allocationId);
            if (!allocationState.unknown()) {
                return;
            }
        }
        log.warn("could not get allocation state for leaf={} output={}", leaf.getId(), leaf.getOutput());
    }

    @Override
    public void visitInnerNode(OutputNode.Internal node) {

        List<AllocationState> allocationStates = new ArrayList<>();
        for (OutputNode outputNode : node.getChildNodes()) {
            outputNode.accept(this);
            allocationStates.add(allocationState);
        }

        allocationState = AllocationState.getNone();
        switch (node.getSemantic()) {
            case redundant:
                allocationState = mergeRedundant(allocationStates);
                break;
            case complementary:
                allocationState = mergeComplementary(allocationStates);
                break;
            case alternative:
                throw new UnsupportedOperationException("not impl");
//                break;
            case concurrent:
                allocationState = mergeComplementary(allocationStates);
                break;
            case sequential:
                throw new UnsupportedOperationException("not impl");
//                break;
            case optional:
                allocationState = mergeOptional(allocationStates);
                break;
        }

    }

    private AllocationState mergeOptional(List<AllocationState> states) {
//        if(states.stream().anyMatch(s -> s.unknown())) {
//            return AllocationState.getNone();
//        }

        if (states.stream().allMatch(s -> s.finished() || s.unknown())) {
            return AllocationState.getSuccess();
        }

        if(states.stream().anyMatch(s -> s.presenting())) {
            return AllocationState.getPresenting();
        }

        if(states.stream().anyMatch(s -> s.initializing())) {
            return AllocationState.getInit();
        }

        if(states.stream().anyMatch(s -> s.cancelling())) {
            return AllocationState.getCancel();
        }

        if(states.stream().allMatch(s -> s.canceled())) {
            return AllocationState.getCanceled();
        }

        throw new IllegalStateException("unhandled presentation state: " +
                states.stream().map(as -> as.toString()).collect(Collectors.joining(" ")));
    }

    private AllocationState mergeComplementary(List<AllocationState> states) {
        if (states.stream().anyMatch(s -> s.failed())) {
            //if one part of the presentation failed, the whole presentation failed
            AllocationState state = states.stream().filter(s -> s.failed()).findAny().get();
            return AllocationState.getError(state.getErrorCause());
        }

        if (states.stream().allMatch(s -> s.successful())) {
            return AllocationState.getSuccess();
        }

        if (states.stream().anyMatch(s -> s.presenting())) {
            return AllocationState.getPresenting();
        }

        if (states.stream().anyMatch(s -> s.cancelling())) {
            return AllocationState.getCancel();
        }

        if (states.stream().allMatch(s -> s.canceled())) {
            return AllocationState.getCanceled();
        }

        if (states.stream().allMatch(s -> s.unknown())) {
            return AllocationState.getNone();
        }

        if (states.stream().filter(s -> s.initializing()).findAny().isPresent()) {
            return AllocationState.getInit();
        }

        if (states.stream().allMatch(s -> s.unknown() || s.finished())) {
            return AllocationState.getError("could not find allocation information for a sub allocation");
        }

        throw new IllegalStateException("unhandled presentation state: " +
                states.stream().map(as -> as.toString()).collect(Collectors.joining(" ")));
    }

    private AllocationState mergeRedundant(List<AllocationState> states) {
        if (states.stream().filter(s -> s.presenting()).findAny().isPresent()) {
            return AllocationState.getPresenting();
        }

        if (states.stream().filter(s -> s.cancelling()).findAny().isPresent()) {
            return AllocationState.getCancel();
        }

        if (states.stream().allMatch(s -> s.unknown())) {
            return AllocationState.getNone();
        }

        if (states.stream().filter(s -> s.initializing()).findAny().isPresent()) {
            return AllocationState.getInit();
        }


        if (states.stream().anyMatch(s -> s.finished())) {
            //at least one sub allocation is finished

            boolean success = states.stream().anyMatch(s -> s.successful());
            if (success) {
                // if one of the redundant presentation was successful the whole presentation is considered successful
                return AllocationState.getSuccess();
            }

            //TODO could merge error message of sub allocations
            return new AllocationState(states.get(0));
        }

        throw new IllegalStateException("unhandled presentation state: " +
                states.stream().map(as -> as.toString()).collect(Collectors.joining(" ")));
    }
}
