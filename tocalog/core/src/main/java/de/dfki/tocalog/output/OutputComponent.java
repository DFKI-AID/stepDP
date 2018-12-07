package de.dfki.tocalog.output;

import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.output.impp.AllocationState;

import java.util.Optional;

/**
 */
public interface OutputComponent {
    /**
     * @param outputUnit
     * @throws IllegalArgumentException
     * @return
     */
    String allocate(Entity outputUnit);

    AllocationState getAllocationState(String id);

    /**
     * @param output
     * @param service
     * @return true iff the output component can present the output on the service
     */
    boolean supports(Entity output, Entity service);
}
