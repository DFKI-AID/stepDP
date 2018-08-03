package de.dfki.tocalog.kb;

import de.dfki.tractat.idl.Base;
import de.dfki.tractat.idl.CborDeserializer;
import de.dfki.tractat.idl.CborSerializer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;
import java.util.stream.*;

/**
 *
 */
public class KnowledgeSet<T extends Base> {
    private String id;
    private Set<T> entries = new HashSet<>();
    private CborSerializer serializer = new CborSerializer();
    private CborDeserializer deserializer = new CborDeserializer();
    private Lock lock = new ReentrantLock();


    public KnowledgeSet(String id) {
        this.id = id;
    }

    public Set<T> getIf(Predicate<T> filter) {
        try {
            lock.lock();
            Set<T> result = new HashSet<>();
            for (T entity : entries) {
                if (filter.test(entity)) {
                    result.add(copy(entity));
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public Set<T> getAll() {
        return getIf((e) -> true);
    }

    public boolean removeIf(Predicate<T> condition) {
        try {
            lock.lock();
            return entries.removeIf(condition);
        } finally {
            lock.unlock();
        }
    }

    public void add(T t) {
        try {
            lock();
            T entityCopy = copy(t);
            entries.add(entityCopy);
        } finally {
            unlock();
        }
    }

    public void apply(Consumer<T> consumer) {
        try {
            lock();
            entries.stream().forEach(consumer);
        } finally {
            unlock();
        }
    }

    public T copy(T base) {
        try {
            lock();
            return (T) base.copy(serializer, deserializer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            unlock();
        }
    }

    public Locked<T> lock() {
        this.lock.lock();
        return locked;
    }

    public void unlock() {
        this.lock.unlock();
    }

    private Stream<T> getStream() {
        return entries.stream();
    }

    interface Locked<T extends Base> {
        Stream<T> getStream();

        void remove(T t);

        default void removeAll(Collection<T> ts) {
            for (T t : ts) {
                remove(t);
            }
        }

        T copy(T base);
    }

    private Locked<T> locked = new Locked<T>() {
        @Override
        public Stream<T> getStream() {
            return getStream();
        }

        @Override
        public void remove(T t) {
            entries.remove(t);
        }

        @Override
        public T copy(T base) {
            try {
                return (T) base.copy(serializer, deserializer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

}
