package de.dfki.tocalog.output.impp;

import de.dfki.tocalog.core.AbstractDialogComponent;
import de.dfki.tocalog.core.Event;
import de.dfki.tocalog.core.EventEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 */
public class OutputCoordinator extends AbstractDialogComponent {
    private PresentableVisitor presentableVisitor = new PresentableVisitor();
    private Map<String, Allocation> allocations = new HashMap<>();

    public String present(OutputNode outputNode) {
        String id = UUID.randomUUID().toString();

        return id;
    }

    public AllocationState getState(String id) {
        if(!allocations.containsKey(id)) {
            return AllocationState.NONE;
        }
        return allocations.get(id).getAllocationState();
    }


    @Override
    public void onEvent(EventEngine engine, Event event) {
    }
}
