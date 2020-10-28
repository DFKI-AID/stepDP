package de.dfki.step.output;

import de.dfki.step.deprecated.kb.Entity;
import de.dfki.step.output.imp.AllocationState;
import de.dfki.step.output.imp.OutputUnit;

/**
 */
public interface OutputComponent {
    /**
     * @param outputUnit
     * @throws IllegalArgumentException
     * @return
     */
    String allocate(OutputUnit outputUnit);

    void deallocate(String allocationId);

    AllocationState getAllocationState(String id);

    /**
     * @param output
     * @param service
     * @return true iff the output component can present the output on the service
     */
    boolean supports(Entity output, Entity service);
}
