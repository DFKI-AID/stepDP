package de.dfki.tocalog.kb;

import de.dfki.tractat.idl.Base;
import de.dfki.tractat.idl.CborDeserializer;
import de.dfki.tractat.idl.CborSerializer;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 */
public class KnowledgeMap<T extends Base> {
    private Map<String, T> store = new HashMap<>();
    private CborSerializer serializer = new CborSerializer();
    private CborDeserializer deserializer = new CborDeserializer();

    public synchronized Optional<T> get(String id) {
        if (!store.containsKey(id)) {
            return Optional.empty();
        }
        T entity = store.get(id);
        Optional<T> entryCopy = Optional.of(copy(entity));
        return entryCopy;
    }

    public synchronized Set<T> getIf(Predicate<T> filter) {
        Set<T> result = new HashSet<>();
        for (T entity : store.values()) {
            if (filter.test(entity)) {
                result.add(copy(entity));
            }
        }
        return result;
    }

    public synchronized Set<T> getAll() {
        return getIf((e) -> true);
    }

    public synchronized boolean removeIf(Predicate<T> condition) {
        return store.values().removeIf(condition);
    }

    public synchronized void put(String id, T t) {
        T entityCopy = copy(t);
        store.put(id, entityCopy);
    }

    public synchronized void apply(Consumer<T> consumer) {
        store.values().stream().forEach(consumer);
    }

    protected synchronized T copy(T base) {
        try {
            return (T) base.copy(serializer, deserializer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
