package de.dfki.tocalog.output;

import de.dfki.tocalog.output.impp.AllocationState;
import de.dfki.tocalog.output.impp.CopyVisitor;
import de.dfki.tocalog.output.impp.OutputNode;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class IMPP implements AllocationModule {
    private OutputNode root;
    private Map<String, OutputNode> allocations = new HashMap<>();
    private Map<String, AllocationState> allocationStates = new HashMap<>();
    private CopyVisitor copyVisitor = new CopyVisitor();

    public IMPP() {
//        root = OutputNode.buildNode(OutputNode.Semantic.concurrent).build();
//        root.
    }

    @Override
    public String allocate(OutputNode output) {
        output = copyVisitor.copy(output).build();

        // AssignServiceVisitor ... a set of them
        // find suitable services for output
        // they are assigned to leaves

        // GetServicesVisitor .. returns all assigned services

        // RemoveServiceVisitor: remove those from output

        // assign best service
        // interface for service -> AllocationModule
        //assign service to each leaf (store mapping)

        //assign id to each intern node
        // create AllocationState for each node
        // create visitor that updates the allocation state

        copyVisitor.copy(output);

        return null;
    }

    @Override
    public AllocationState getState(String id) {

        if(!allocationStates.containsKey(id)) {
            return AllocationState.NONE;
        }
        //return allocations.get(id).getAllocationState();
        return null;
    }
}
