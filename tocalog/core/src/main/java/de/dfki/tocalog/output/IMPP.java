package de.dfki.tocalog.output;

import de.dfki.tocalog.kb.KnowledgeBase;
import de.dfki.tocalog.output.impp.*;

import java.util.*;

/**
 */
public class IMPP {
    private final KnowledgeBase kb;
    private OutputNode root;
    private Map<String, OutputNode> allocations = new HashMap<>();
    private Map<String, AllocationState> allocationStates = new HashMap<>();
    private CopyVisitor copyVisitor = new CopyVisitor();
    private Set<OutputComponent> components = new HashSet<>();

    public IMPP(KnowledgeBase kb) {
        this.kb = kb;
//        root = OutputNode.buildNode(OutputNode.Semantic.concurrent).create();
//        root.
    }

    public Allocation allocate(OutputNode output) { //TODO OutputRequest? what, when, whom? how?
        //output = copyVisitor.copy(output).build();


        FindCandidateVisitor cf = new FindCandidateVisitor(this);
        Map<String, Assignment> assignments = cf.visit(output);

        AllocateVisitor av = new AllocateVisitor(this);
        Allocation allocation = av.visit(output, assignments);

        return allocation;

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

        //copyVisitor.copy(output);

//        return null;
    }


    public AllocationState getState(String id) {

        if (!allocationStates.containsKey(id)) {
            return AllocationState.NONE;
        }
        //return allocations.get(id).getAllocationState();
        return null;
    }

//    @Override
//    public boolean handles(Service service) {
//        for (OutputComponent component : components) {
//            if (component.handles(service)) {
//                return true;
//            }
//        }
//        return false;
//    }

    public void addOutputComponent(OutputComponent component) {
        this.components.add(component);
    }

    public Set<OutputComponent> getComponents() {
        return components;
    }

    public KnowledgeBase getKb() {
        return kb;
    }
}
