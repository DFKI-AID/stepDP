package de.dfki.step.deprecated.kb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Represents a single entity in a {@link DataStore} like a human.
 * You may extend this class to add specific getter for your use case.
 * See the unit test (DataStoreTest) for usage.
 */
@Deprecated
public class DataEntry {
    private static final Logger log = LoggerFactory.getLogger(DataEntry.class);
    private final DataView dataView;
    private final String id;

    public DataEntry(DataStore<Object> dataStore, String id) {
        this.dataView = new DataView(dataStore);
        this.id = id;
    }

    public DataEntry(DataView dataView, String id) {
        this.dataView = dataView;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Optional<Object> get(String key) {
        return dataView.get(id + "." + key);
    }

    public <S> Optional<S> get(String key, Class<S> clazz) {
        return dataView.get(id + "." + key, clazz);
    }


    public void set(String key, Object value) {
        dataView.set(id + "." + key, value);
    }

    public void save() {
        dataView.save();
    }

    public void reload() {
        dataView.reload();
    }

    public DataView getDataView() {
        return dataView;
    }
}
