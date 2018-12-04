package de.dfki.tocalog.output;

import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.output.impp.AllocationState;

import java.util.Optional;

/**
 */
public interface OutputComponent {
    /**
     * @param output
     * @return
     */
    String allocate(Entity output, Entity service);

    AllocationState getAllocationState(String id);

    boolean handles(Entity output, Entity service);
}
