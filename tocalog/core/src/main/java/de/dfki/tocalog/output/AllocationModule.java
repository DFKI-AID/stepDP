package de.dfki.tocalog.output;

import de.dfki.tocalog.output.impp.AllocationState;

/**
 */
public interface AllocationModule<T extends Output> {
    /**
     *
     * @param output
     * @return
     */
    String allocate(AllocationRequest<T> output);

    AllocationState getState(String id);
}
