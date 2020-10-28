package de.dfki.step.deprecated.kb;

import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@Deprecated
public class DataView {
    private static final Logger log = LoggerFactory.getLogger(DataView.class);
    private final DataStore<Object> dataStore;
    private PMap<String, Object> values;
    private List<Change> changes = new ArrayList<>();

    public DataView(DataStore<Object> dataStore) {
        this.dataStore = dataStore;
        this.values = dataStore.values;
    }

    public Optional<Object> get(String id) {
        return Optional.ofNullable(this.values.get(id));
    }

    public <S> Optional<S> get(String id, Class<S> clazz) {
        return retrieve(id, clazz);
    }

    private <S> Optional<S> retrieve(String id, Class<S> clazz) {
        return get(id)
                .filter(x -> clazz.isAssignableFrom(x.getClass()))
                .map(x -> (S) x);
    }

    public void set(String id, Object value) {
        Change.Type type;
        if (!this.values.containsKey(id)) {
            type = Change.Type.ADD;
        } else {
            type = Change.Type.UPDATE;
        }
        this.values = this.values.plus(id, value);
        this.changes.add(new Change(id, type));
    }

    /**
     * Updates the underlying {@link DataStore} by reflecting your changes (add, remove, update)
     * you did via this view.
     */
    public void save() {
        synchronized (dataStore) {
            for (Change change : changes) {
                switch (change.type) {
                    case ADD:
                    case UPDATE:
                        dataStore.add(change.id, values.get(change.id));
                        break;
                    case REMOVE:
                        dataStore.remove(change.id);
                        break;
                }
            }
            reload();
        }
    }

    /**
     * Discard all changes done and reload from the underlying {@link DataStore}.
     */
    public void reload() {
        this.values = dataStore.values;
        this.changes.clear();
    }


    private static class Change {
        public Change(String id, Type type) {
            this.id = id;
            this.type = type;
        }

        public enum Type {
            ADD,
            REMOVE,
            UPDATE
        }

        protected final String id;
        protected final Type type;


    }
}
