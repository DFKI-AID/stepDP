package de.dfki.step.deprecated.kb;

import de.dfki.step.core.Component;
import de.dfki.step.core.ComponentManager;
import org.pcollections.PMap;

/**
 * Grants access to a global {@link DataStore}
 */
@Deprecated
public class DataStoreComponent extends DataStore implements Component  {

    @Override
    public void init(ComponentManager cm) {

    }

    @Override
    public void deinit() {

    }

    @Override
    public void update() {

    }

    @Override
    public Object createSnapshot() {
        return values;
    }

    @Override
    public void loadSnapshot(Object snapshot) {
       setValues((PMap) snapshot);
    }

    public DataEntry getEntry(String id) {
        return new DataEntry(this, id);
    }
}
