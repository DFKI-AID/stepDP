package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.output.Imp;

import java.util.Map;

/**
 */
public class Allocation {
    private final Map<String, String> allocationsIds;
    private final Imp impp;
    private OutputNode outputNode;
    private AllocationState allocationState;

    public Allocation(Imp impp, OutputNode outputNode, Map<String, String> allocationsIds) {
        this.impp = impp;
        this.outputNode = outputNode;
        this.allocationsIds = allocationsIds;
        this.allocationState = AllocationState.NONE;
    }

    public synchronized AllocationState getAllocationState() {
        return allocationState;
    }

    public void updateAllocationState() {
        AllocationStateVisitor asv = new AllocationStateVisitor();
        this.allocationState = asv.visit(this);
    }

    public Map<String, String> getAllocationsIds() {
        return allocationsIds;
    }

    public Imp getImpp() {
        return impp;
    }

    public OutputNode getOutputNode() {
        return outputNode;
    }
}
