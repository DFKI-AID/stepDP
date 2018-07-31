package de.dfki.tocalog.output;

import de.dfki.tocalog.output.impp.AllocationState;
import de.dfki.tocalog.output.impp.OutputNode;

/**
 */
public class IMPP implements AllocationModule {
//    private InnerNode root;

    public IMPP() {
//        root = OutputNode.buildNode(OutputNode.Semantic.concurrent).build();
//        root.
    }

    @Override
    public String allocate(OutputNode output) {
        return null;
    }

    @Override
    public AllocationState getState(String id) {
        return null;
    }
}
