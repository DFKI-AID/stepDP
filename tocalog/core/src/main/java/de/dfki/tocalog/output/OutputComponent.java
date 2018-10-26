package de.dfki.tocalog.output;

import de.dfki.tocalog.output.impp.AllocationState;

/**
 */
public interface OutputComponent {
    /**
     * @param output
     * @return
     */
    String allocate(Output output, String service);

    AllocationState getState(String id);

    boolean handles(Output output, String service);
}
