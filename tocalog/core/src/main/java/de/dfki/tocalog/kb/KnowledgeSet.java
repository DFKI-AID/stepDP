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
public class KnowledgeSet<T extends Base> {
    private String id;
    private Set<T> entries = new HashSet<>();
    private CborSerializer serializer = new CborSerializer();
    private CborDeserializer deserializer = new CborDeserializer();

    public KnowledgeSet(String id) {
        this.id = id;
    }

    public synchronized Set<T> getIf(Predicate<T> filter) {
        Set<T> result = new HashSet<>();
        for (T entity : entries) {
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
        return entries.removeIf(condition);
    }

    public synchronized void add( T t) {
        T entityCopy = copy(t);
        entries.add(entityCopy);
    }

    public synchronized void apply(Consumer<T> consumer) {
        entries.stream().forEach(consumer);
    }

    protected synchronized T copy(T base) {
        try {
            return (T) base.copy(serializer, deserializer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
