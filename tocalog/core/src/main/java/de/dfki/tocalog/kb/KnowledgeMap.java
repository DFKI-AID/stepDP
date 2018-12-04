package de.dfki.tocalog.kb;

import org.pcollections.HashPMap;
import org.pcollections.IntTreePMap;
import org.pcollections.PMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * TODO maybe: associate Type with map -> throw exception on illegal type && add type if not present
 */
public class KnowledgeMap {
    private PMap<String, Entity> entities = HashPMap.empty(IntTreePMap.empty());
    private KMHistory history = new KMHistory(1000); //TODO count

    /**
     * Adds an entity to the knowledge map and overwrites any existing entity with the same id (see Ontology::id).
     * An id is generated if not present.
     *
     * @param entity
     * @return The (generated) id if the added entity
     */
    public synchronized String add(Entity entity) {
        Optional<String> id = entity.get(Ontology.id);
        if (!id.isPresent()) {
            id = Optional.of(UUID.randomUUID().toString());
            entity = entity.set(Ontology.id, id.get());
//            throw new IllegalArgumentException("need id for putting entity into kb");
        }
        this.entities = this.entities.plus(id.get(), entity);
        history.addEntry(entity);
        return id.get();
    }

    /**
     * Updates the given attributes for the entity in the knowledge map.
     * If no entity is available (or id is not presented) a new entity is generated.
     * This method can be used to updates certain attributes of an entity without overwriting
     * changes from other components.
     *
     * @param entity
     * @param attributes
     * @return
     */
    public synchronized String add(Entity entity, Attribute... attributes) {
        String id = entity.get(Ontology.id)
                .orElse(UUID.randomUUID().toString());

        Entity kbEnt;
        if (this.entities.containsKey(id)) {
            kbEnt = this.entities.get(id);
        } else {
            kbEnt = new Entity();
            kbEnt.set(Ontology.id, id);
        }

        for (Attribute attr : attributes) {
            Optional optValue = entity.get(attr);
            if (!optValue.isPresent()) {
                throw new IllegalStateException("value is not present for " + attr);
            }
            kbEnt = kbEnt.set(attr, optValue.get());
        }

        this.entities = this.entities.plus(id, kbEnt);
        history.addEntry(kbEnt);
        return id;
    }

    public Collection<Entity> getAll() {
        return this.entities.values();
    }

    public Map<String, Entity> getStore() {
        return this.entities;
    }

    /**
     * TODO rename to set
     * Updates the value of one attribute of an entity in the knowledge map if the entity is present.
     *
     * @param id
     * @param attr
     * @param value
     * @param <T>
     * @return true iff the entity was present
     */
    public synchronized <T> boolean update(String id, Attribute<T> attr, T value) {
        if (!this.entities.containsKey(id)) {
            //TODO could also of a new entity
            return false;
        }
        Entity entity = this.entities.get(id);
        entity = entity.set(attr, value);
        this.entities = this.entities.plus(id, entity);
        history.updateEntry(entity);
        return true;
    }

    public synchronized void update(Function<Entity, Entity> updateFnc) {
        for (Map.Entry<String, Entity> entry : entities.entrySet()) {
            Entity entity = updateFnc.apply(entry.getValue());
            if (entity == entry.getValue()) {
                //no update
                continue;
            }
            this.entities = this.entities.plus(entry.getKey(), entity);
            history.updateEntry(entity);
        }
    }

    public synchronized <T> boolean unset(String id, Attribute<T>... attrs) {
        if(!this.entities.containsKey(id)) {
            return false;
        }
        Entity e = this.entities.get(id);
        for(Attribute attr : attrs) {
            e = e.unset(attr);
        }
        this.entities = this.entities.plus(id, e);
        return true;
    }

    public Optional<Entity> get(String id) {
        return Optional.ofNullable(this.entities.get(id));
    }


    public Collection<Entity> query(Predicate<Entity> predicate) {
        Collection<Entity> queryResult = this.entities.values().stream()
                .filter(e -> predicate.test(e))
                .collect(Collectors.toList());
        return queryResult;
    }

    public synchronized void removeIf(Predicate<Entity> predicate) {
        PMap<String, Entity> newEntities = entities;
        for (Map.Entry<String, Entity> entry : newEntities.entrySet()) {
            if (predicate.test(entry.getValue())) {
                newEntities = newEntities.minus(entry.getKey());
                history.removeEntry(entry.getValue());
            }
        }
        this.entities = newEntities;
    }


    public void removeOld(long timeout) {
        long now = System.currentTimeMillis();
        removeIf(ent -> ent.get(Ontology.timestamp).orElse(0l) + timeout < now);
    }


    public void consume(Consumer<Entity> consumer) {
        entities.values().stream().forEach(consumer);
    }


    public synchronized void updateTimestamp(Predicate<Entity> pred) {
        long now = System.currentTimeMillis();
        this.update(ent -> {
            if (!pred.test(ent)) {
                return ent;
            }
            ent = ent.set(Ontology.timestamp, now);
            return ent;
        });
    }

    /**
     * @param source
     * @return All entities from with the given source.
     */
    public Collection<Entity> getFromSource(String source) {
        Set<Entity> entries = this.entities.entrySet().stream()
                .map(e -> e.getValue())
                .filter(e -> e.get(Ontology.source).orElse("").equals(source))
                .collect(Collectors.toSet());
        return entries;
    }


    /**
     * Limits the number of entries inside the set: This method will remove entries such that the total count of
     * entries is less than the given size.
     *
     * @param maxCount
     * @param comparator
     */
    public synchronized void limit(int maxCount, Comparator<? super Entity> comparator) {
        if (entities.size() < maxCount) {
            return;
        }

        List<Entity> ordered = entities.entrySet().stream()
                .map(e -> e.getValue())
                .sorted(comparator).collect(Collectors.toList());
        maxCount = Math.min(maxCount, ordered.size());
        Iterator<Entity> iter = ordered.iterator();
        while (entities.size() > maxCount) {
            Entity entity = iter.next();
            this.entities.minus(entity);
            history.removeEntry(entity);
        }
    }

    public KMHistory getHistory() {
        return history;
    }
}
