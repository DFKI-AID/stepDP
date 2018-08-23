package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.output.IMPP;

import java.util.Map;

/**
 */
public class Allocation {
    private final Map<String, String> allocationsIds;
    private final IMPP impp;
    private OutputNode outputNode;
    private AllocationState allocationState;

    public Allocation(IMPP impp, OutputNode outputNode, Map<String, String> allocationsIds) {
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

    public IMPP getImpp() {
        return impp;
    }

    public OutputNode getOutputNode() {
        return outputNode;
    }
}
