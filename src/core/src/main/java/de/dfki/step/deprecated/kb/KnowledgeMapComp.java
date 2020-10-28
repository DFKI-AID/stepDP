package de.dfki.step.deprecated.kb;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import org.pcollections.PMap;

@Deprecated
public class KnowledgeMapComp  extends KnowledgeMap implements Component {
    @Override
    public void init(ComponentManager cm) {
        this.clear();
    }

    @Override
    public void deinit() {
    }

    @Override
    public void update() {
    }

    @Override
    public synchronized Object createSnapshot() {
        return entities;
    }

    @Override
    public synchronized void loadSnapshot(Object snapshot) {
        entities = (PMap<String, Entity>) snapshot;
    }
}
