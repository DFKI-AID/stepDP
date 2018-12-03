package de.dfki.tocalog.kb;

import okhttp3.Call;
import org.pcollections.*;

/**
 * TODO rename: RecordHistory?
 * Keeps a history of changes done in a {@link KnowledgeMap}
 */
public class KMHistory {
    private final long maxEntryCount;
    private PQueue<Entry> history = AmortizedPQueue.empty();
    private PSequence<Callback> callbacks = TreePVector.empty();

    public interface Callback {
        void on(Entry entry);
    }

    public KMHistory(long maxEntryCount) {
        this.maxEntryCount = maxEntryCount;
        if (this.maxEntryCount <= 0) {
            throw new IllegalArgumentException("maxEntryCount has to be greater than 0");
        }
    }

    public enum EntryType {
        ADD,
        UPDATE,
        REMOVE
    }

    public static class Entry {
        public final EntryType type;
        public final Entity entity;

        private Entry(EntryType type, Entity entity) {
            this.type = type;
            this.entity = entity;
        }
    }

    private void shrink() {
        while (history.size() > maxEntryCount) {
            history = history.minus();
        }
    }

    protected void addEntry(Entity entity) {
        Entry entry;
        synchronized (this) {
            entry = new Entry(EntryType.ADD, entity);
            this.history = this.history.plus(entry);
            shrink();
        }
        updateCallbacks(entry);
    }

    protected synchronized void removeEntry(Entity entity) {
        Entry entry;
        synchronized (this) {
            entry = new Entry(EntryType.REMOVE, entity);
            this.history = this.history.plus(entry);
        }
        updateCallbacks(entry);
    }

    protected synchronized void updateEntry(Entity entity) {
        Entry entry;
        synchronized (this) {
            entry = new Entry(EntryType.UPDATE, entity);
            this.history = this.history.plus(entry);
            shrink();
        }
        updateCallbacks(entry);
    }

    public synchronized void addCallback(Callback callback) {
        this.callbacks = callbacks.plus(callback);
    }

    public synchronized void removeCallback(Callback callback) {
        this.callbacks = callbacks.minus(callback);
    }

    protected void updateCallbacks(Entry entry) {
        for(Callback callback : callbacks) {
            callback.on(entry);
        }
    }

    public PQueue<Entry> getHistory() {
        return history;
    }
}
