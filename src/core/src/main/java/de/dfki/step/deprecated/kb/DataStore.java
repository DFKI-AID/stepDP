package de.dfki.step.deprecated.kb;

import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import org.pcollections.IntTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class store arbitrary java objects in a persistent map.
 * Stored values should be immutable or persistent. {@link #verify} and {@link #checkMutability(boolean)}
 * may help to check for inconsistencies.
 *
 * The general idea is split up the information of an entity into multiple parts. For example,
 * the position is stored independent from other information of the same entity. However,
 * the position is stored similar for all entities.
 *
 * This approach has the following benefits:
 * (1) Multiple parallel reads: Sync is only necessary for update
 * (2) Parallel write: If multiple components write on different keys, no write will be lost
 * (3) Only one class {@link DataEntry} has to managed.
 *
 */
@Deprecated
public class DataStore<T> {
    private static final Logger log = LoggerFactory.getLogger(DataStore.class);
    private boolean checkMutability = false;
    protected PMap<String, T> values = HashPMap.empty(IntTreePMap.empty());
    protected PMap<String, Integer> hashes = HashTreePMap.empty();

    /**
     * Adds an entity to the knowledge map and overwrites any existing entity with the same id (see DataEntry::id).
     * An id is generated if not present.
     *
     * @param entity
     * @return The (generated) id if the added entity
     */
    public synchronized void add(String id, T entity) {
        if(checkMutability) {
            verify();
        }

        T currentEntry = values.get(id);
        boolean updating = currentEntry != null && !Objects.equals(currentEntry, entity);

        if(updating) {
            log.info("updating {}: {}", id, entity);
        } else {
            log.info("adding {}: {}", id, entity);
        }

        this.values = this.values.plus(id, entity);
        this.hashes = this.hashes.plus(id, entity.hashCode());

        if(updating) {
            onUpdate(this.values.get(id), entity);
        } else {
            onAdd(entity);
        }
    }

    public DataStore() {
    }

    public DataStore(PMap<String, T> values) {
        setValues(values);
    }

    public void setValues(PMap<String, T> values) {
        this.values = values;
        values.entrySet().forEach(v -> hashes = hashes.plus(v.getKey(), v.hashCode()));
    }

    public Collection<T> getAll() {
        return this.values.values();
    }

    public Map<String, T> getStore() {
        return this.values;
    }

    public Stream<Map.Entry<String, T>> stream() {
        return this.values.entrySet().stream();
    }

    public Stream<String> primaryIds() {
        Pattern primaryKeyPattern = Pattern.compile("(.*)\\..*");
        return this.values.keySet().stream()
                .map(k ->  {
                    Matcher matcher = primaryKeyPattern.matcher(k);
                    if(!matcher.matches()) {
                        return "";
                    }
                    return matcher.group(1);
                })
                .filter(k -> !k.isEmpty());
    }


    public synchronized void add(Function<T, T> updateFnc) {
        if(checkMutability) {
            verify();
        }

        for (Map.Entry<String, T> entry : values.entrySet()) {
            T entity = updateFnc.apply(entry.getValue());
            if (entity == entry.getValue()) {
                //no add
                continue;
            }
            log.info("updating {}", entity);
            String id = entry.getKey();
            this.values = this.values.plus(id, entity);
            this.hashes = this.hashes.plus(id, entity.hashCode());
            onUpdate(entry.getValue(), entity);
        }
    }

    public Optional<T> get(String id) {
        return Optional.ofNullable(this.values.get(id));
    }

    public <S extends T> Optional<S> get(String id, Class<S> clazz) {
        if(checkMutability) {
            verify();
        }

        return get(id)
                .filter(x -> clazz.isAssignableFrom(x.getClass()))
                .map(x -> (S) x);
    }

    public Collection<T> query(Predicate<T> predicate) {
        if(checkMutability) {
            verify();
        }

        Collection<T> queryResult = this.values.values().stream()
                .filter(e -> predicate.test(e))
                .collect(Collectors.toList());
        return queryResult;
    }

    public synchronized boolean remove(String id) {
        if(checkMutability) {
            verify();
        }

        if(!values.containsKey(id)) {
            return false;
        }
        log.info("removing {}", id);
        values = values.minus(id);
        hashes = hashes.minus(id);
        return true;
    }

    public synchronized void removeIf(Predicate<T> predicate) {
        if(checkMutability) {
            verify();
        }

        PMap<String, T> newEntities = values;
        for (Map.Entry<String, T> entry : newEntities.entrySet()) {
            if (predicate.test(entry.getValue())) {
                log.info("removing {}", entry.getKey());
                String id = entry.getKey();
                newEntities = newEntities.minus(id);
                hashes = hashes.minus(id);
                onRemove(entry.getValue());
            }
        }
        this.values = newEntities;
    }


    public void consume(Consumer<T> consumer) {
        if(checkMutability) {
            verify();
        }

        values.values().stream().forEach(consumer);
    }


    /**
     * Limits the number of entries inside the set: This method will remove entries such that the total count of
     * entries is less than the given size.
     *
     * @param maxCount
     * @param comparator
     */
    public synchronized void limit(int maxCount, Comparator<T> comparator) {
        if(checkMutability) {
            verify();
        }

        if (values.size() < maxCount) {
            return;
        }

        List<Map.Entry<String, T>> ordered = values.entrySet().stream()
                .sorted((x, y) -> comparator.compare(x.getValue(), y.getValue()))
                .collect(Collectors.toList());

        maxCount = Math.min(maxCount, ordered.size());
        Iterator<Map.Entry<String, T>> iter = ordered.iterator();
        while (values.size() > maxCount) {
            Map.Entry<String, T> entry = iter.next();
            String id = entry.getKey();
            values = values.minus(id);
            hashes = hashes.minus(id);
            onRemove(entry.getValue());
        }
    }

    protected void onRemove(T value) {
    }

    protected void onUpdate(T oldValue, T newValue) {
    }

    protected void onAdd(T value) {
    }

    /**
     * Remove all entries.
     */
    public void clear() {
        this.values = HashTreePMap.empty();
        this.hashes = HashTreePMap.empty();
    }

    /**
     * Checks whether the current values still match the hashes.
     * This method should only be used for debugging and may help to find out
     * whether non-persistent data structures are used.
     */
    public synchronized void verify() {
        for(Map.Entry<String, T> entry : values.entrySet()) {
            String id = entry.getKey();
            int expectedHash = hashes.get(id);
            int actualHash = entry.getValue().hashCode();
            if(Objects.equals(expectedHash, actualHash)) {
                continue;
            }

            log.error("Unexpected value change in DataStore for {}={}", id, entry.getValue());
            throw new IllegalStateException(
                    String.format("Unexpected value change in DataStore for %s=%s",  id, entry.getValue()));
        }
    }

    public synchronized void checkMutability(boolean checkMutability) {
        this.checkMutability = checkMutability;
    }
}
