package de.dfki.tocalog.kb;

import de.dfki.sire.Base;
import de.dfki.sire.CborDeserializer;
import de.dfki.sire.CborSerializer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
            return (T) base.copy();
        } finally {
            unlock();
        }
    }

    public Locked<T> lock() {
        this.lock.lock();
        return locked;
    }

    public Optional<Locked<T>> tryLock(long timeout, TimeUnit unit) {
        try {
            if (this.lock.tryLock(timeout, unit)) {
                return Optional.of(locked);
            }
            return Optional.empty();
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    public void unlock() {
        this.lock.unlock();
    }

    private Stream<T> getStream() {
        return entries.stream();
    }


    private Locked<T> locked = new Locked<>(this);

    public static class Locked<T extends Base> {
        private KnowledgeSet<T> ks;

        public Locked(KnowledgeSet<T> ks) {
            this.ks = ks;
        }

        public Stream<T> getStream() {
            return ks.getStream();
        }

        public Collection<T> getData() {
            return ks.entries;
        }

        public void remove(T t) {
            ks.entries.remove(t);
        }

        public T copy(T base) {
            return (T) base.copy();
        }

        public int getSize() {
            return ks.entries.size();
        }

        public void removeAll(Collection<T> c) {
            ks.entries.removeAll(c);
        }
    }

    ;

}
