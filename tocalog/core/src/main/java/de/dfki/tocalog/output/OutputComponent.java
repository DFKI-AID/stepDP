package de.dfki.tocalog.output;

import de.dfki.tocalog.model.Service;
import de.dfki.tocalog.output.impp.AllocationState;

/**
 */
public interface OutputComponent {
    /**
     * @param output
     * @return
     */
    String allocate(Output output, Service service);

    AllocationState getState(String id);

    boolean handles(Output output, Service service);
}
