package de.dfki.tocalog.output;

import de.dfki.tocalog.output.impp.AllocationState;
import de.dfki.tocalog.output.impp.OutputNode;

/**
 */
public interface AllocationModule {
    /**
     * @param output
     * @return
     */
    String allocate(OutputNode output);

    AllocationState getState(String id);
}
