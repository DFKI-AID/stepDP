package de.dfki.step.output;

import de.dfki.step.kb.Entity;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.output.impp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 */
public class Imp {
    private static Logger log = LoggerFactory.getLogger(Imp.class);
    private final KnowledgeBase kb;
    private OutputNode root;
    private Map<String, OutputNode> allocations = new HashMap<>();
    private Map<String, AllocationState> allocationStates = new HashMap<>();
    private CopyVisitor copyVisitor = new CopyVisitor();
    private Set<OutputComponent> components = new HashSet<>();

    public Imp(KnowledgeBase kb) {
        this.kb = kb;
//        root = OutputNode.build(OutputNode.Semantic.concurrent).of();
//        root.
    }

    public void cancel(Allocation allocation) {
        //TODO impl
    }

    public Allocation allocation(Output output) {
        //TODO
        return null;
    }

    public Allocation allocate(OutputNode output) { //TODO OutputRequest? what, when, whom? how?
        //output = copyVisitor.copy(output).of();

        var fcv = new FindCandidateVisitor(this);
        Map<String, OutputUnit> candidates = fcv.visit(output);


        //TODO is feasible visitor for early failure


        AllocateVisitor av = new AllocateVisitor(this);
        Allocation allocation = av.visit(output, candidates);



//        FindCandidateVisitor cf = new FindCandidateVisitor(this);
//        Map<String, Assignment> assignments = cf.visit(output);
//
//        AllocateVisitor av = new AllocateVisitor(this);
//        Allocation allocation = av.visit(output, assignments);

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
        // of AllocationState for each node
        // of visitor that updates the allocation state

        //copyVisitor.copy(output);

//        return null;
    }


    public AllocationState getState(Allocation allocation) {
        AllocationStateVisitor vis = new AllocationStateVisitor();
        AllocationState state = vis.visit(allocation);
        return state;

//        if (!allocationStates.containsKey(id)) {
//            return AllocationState.NONE;
//        }
//        //return allocations.get(id).getAllocationState();
//        return null;
    }

//    @Override
//    public boolean supports(Service service) {
//        for (OutputComponent component : components) {
//            if (component.supports(service)) {
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

    public boolean supports(Entity output, Entity service) {
        for(OutputComponent oc : components) {
            if(oc.supports(output, service)) {
                return true;
            }
        }
        return false;
    }
}
