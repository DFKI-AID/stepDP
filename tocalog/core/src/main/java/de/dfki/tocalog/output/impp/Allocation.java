package de.dfki.tocalog.output.impp;

/**
 */
public class Allocation {
    private OutputNode outputNode;
    private AllocationState allocationState;

    public Allocation(OutputNode outputNode) {
        this.outputNode = outputNode;
        this.allocationState = AllocationState.NONE;
    }

    public synchronized AllocationState getAllocationState() {
        return allocationState;
    }
}
