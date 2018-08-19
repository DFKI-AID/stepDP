package de.dfki.tocalog.output;

import de.dfki.tocalog.model.Service;
import de.dfki.tocalog.output.impp.AllocationState;
import de.dfki.tocalog.output.impp.OutputNode;

/**
 */
public interface OutputComponent {
    /**
     * @param output
     * @return
     */
    String allocate(OutputNode output);

    AllocationState getState(String id);

    boolean handles(Service service);
}
