package de.dfki.tocalog.kb;

import de.dfki.sire.Base;
import de.dfki.sire.CborDeserializer;
import de.dfki.sire.CborSerializer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 */
public class KnowledgeMap<T extends Base> {
    private Map<String, T> store = new HashMap<>();
    private CborSerializer serializer = new CborSerializer();
    private CborDeserializer deserializer = new CborDeserializer();
    private Lock lock = new ReentrantLock();


    public Optional<T> get(String id) {
        try {
            lock();
            if (!store.containsKey(id)) {
                return Optional.empty();
            }
            T entity = store.get(id);
            Optional<T> entryCopy = Optional.of(copy(entity));
            return entryCopy;
        } finally {
            unlock();
        }
    }

    public Set<T> get(Set<String> ids) {
        Set<T> result = new HashSet<>();
        try {
            lock();
            for (String id : ids) {
                if (store.containsKey(id)) {
                    result.add(store.get(id));
                }
            }
        } finally {
            unlock();
        }
        return result;
    }

    public Optional<T> getAny(Predicate<T> predicate) {
        try {
            lock();
            for (T entity : store.values()) {
                if (predicate.test(entity)) {
                    return Optional.of((T) entity.copy());
                }
            }
        } finally {
            unlock();
        }
        return Optional.empty();
    }

    public Set<T> getIf(Predicate<T> predicate) {
        try {
            lock();
            Set<T> result = new HashSet<>();
            for (T entity : store.values()) {
                if (predicate.test(entity)) {
                    result.add(copy(entity));
                }
            }
            return result;
        } finally {
            unlock();
        }
    }

    public Set<T> getAll() {
        try {
            lock();
            Set<T> result = new HashSet<>();
            for (T entity : store.values()) {
                result.add(copy(entity));
            }
            return result;
        } finally {
            unlock();
        }
    }
//
//    public synchronized Set<T> getAll() {
//        return getIf((e) -> true);
//    }
//
//    public synchronized boolean removeIf(Predicate<T> condition) {
//        return store.values().removeIf(condition);
//    }

    public void put(String id, T t) {
        try {
            lock();
            T entityCopy = copy(t);
            store.put(id, entityCopy);
        } finally {
            unlock();
        }
    }

    public void apply(Consumer<T> consumer) {
        try {
            lock();
            store.values().stream().forEach(consumer);
        } finally {
            unlock();
        }
    }

    protected synchronized T copy(T base) {
        return (T) base.copy();
    }

    public KnowledgeMap.Locked<T> lock() {
        this.lock.lock();
        return locked;
    }

    public void unlock() {
        this.lock.unlock();
    }

    private Stream<Map.Entry<String, T>> getStream() {
        return store.entrySet().stream();
    }


    private Locked<T> locked = new Locked<>(this);

    public static class Locked<T extends Base> {
        private KnowledgeMap<T> km;

        public Locked(KnowledgeMap km) {
            this.km = km;
        }

        public Stream<Map.Entry<String, T>> getStream() {
            return km.getStream();
        }

        public Map<String, T> getData() {
            return km.store;
        }

        public void remove(String id) {
            km.store.remove(id);
        }

        public T copy(T base) {
            return km.copy(base);
        }
    }

}