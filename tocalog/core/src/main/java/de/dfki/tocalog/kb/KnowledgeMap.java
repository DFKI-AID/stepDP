package de.dfki.tocalog.kb;

import de.dfki.sire.CborDeserializer;
import de.dfki.sire.CborSerializer;
import org.pcollections.HashPMap;
import org.pcollections.IntTreePMap;
import org.pcollections.PMap;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 */
public class KnowledgeMap {
    private CborSerializer serializer = new CborSerializer();
    private CborDeserializer deserializer = new CborDeserializer();
    private Lock lock = new ReentrantLock();
    private Random rdm = new Random();

    private PMap<String, Entity> entities = HashPMap.empty(IntTreePMap.empty());

    public synchronized String add(Entity entity) {
        Optional<String> id = entity.get(Ontology.id);
        if (!id.isPresent()) {
            id = Optional.of(UUID.randomUUID().toString());
            entity = entity.set(Ontology.id, id.get());
//            throw new IllegalArgumentException("need id for putting entity into kb");
        }
        this.entities = this.entities.plus(id.get(), entity);
        return id.get();
    }

    public synchronized String add(Entity entity, Attribute... attributes) {
        Optional<String> optId = entity.get(Ontology.id);
        String id = optId.orElse(UUID.randomUUID().toString());

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
                throw new IllegalStateException("value not presented for " + attr);
            }
            kbEnt = kbEnt.set(attr, optValue.get());
        }

        this.entities = this.entities.plus(optId.get(), kbEnt);
        return id;
    }

    public Collection<Entity> getAll() {
        return this.entities.values();
    }

    public Map<String, Entity> getStore() {
        return this.entities;
    }

    public synchronized <T> void update(String id, Attribute<T> attr, T value) {
        if (!this.entities.containsKey(id)) {
            //TODO could also of a new entity
            return;
        }
        Entity entity = this.entities.get(id);
        entity = entity.set(attr, value);
        this.entities = this.entities.plus(id, entity);
    }


    //TODO optional or empty ent?
    public Optional<Entity> get(String id) {
        return Optional.ofNullable(this.entities.get(id));
    }


    public Collection<Entity> query(Predicate<Entity> predicate) {
        Collection<Entity> queryResult = this.entities.values().stream()
                .filter(e -> predicate.test(e))
                .collect(Collectors.toList());
        return queryResult;
    }

    public synchronized void removeif(Predicate<Entity> predicate) {
        PMap<String, Entity> newEntities = entities;
        for (Map.Entry<String, Entity> entry : newEntities.entrySet()) {
            if (predicate.test(entry.getValue())) {
                newEntities = newEntities.minus(entry.getKey());
            }
        }
        this.entities = newEntities;
    }


    public void removeOld(long timeout) {
        long now = System.currentTimeMillis();
        removeif(ent -> ent.get(Ontology.timestamp).orElse(0l) + timeout < now);
    }


    public void consume(Consumer<Entity> consumer) {
        entities.values().stream().forEach(consumer);
    }


    public synchronized void updateTimestamp(Predicate<Entity> pred) {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Entity> entry : entities.entrySet()) {
            Entity entity = entry.getValue().set(Ontology.timestamp, now);
            this.entities = this.entities.plus(entry.getKey(), entity);
        }

    }

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
            this.entities.minus(iter.next());
        }
    }

//    /**
//     * @param maxCount
//     */
//    public void limitTimestamp(int maxCount) {
//        limit(maxCount, Comparator.comparing(e -> e.getTimestamp().orElse(0l)));
//    }


    /**
     * e.g. store multiple alternative sentences in the map
     *
     * @return A random entity from this map.
     */
//    public synchronized Optional<Ontology.Ent> getRandom() {
//        if (entities.isEmpty()) {
//            return Optional.empty();
//        }
//        Iterator<Ontology.Ent> iter = entities.values().iterator();
//        int index = rdm.nextInt(entities.size());
//        for (int i = 0; i < index - 1; i++) {
//            iter.next();
//        }
//        return Optional.of(iter.next());
//    }

//    public void unlock() {
//        this.lock.unlock();
//    }


//    private Locked<T> locked = new Locked<>(this);
//
//    public static class Locked<T extends Base> {
//        private KnowledgeMap<T> km;
//
//        public Locked(KnowledgeMap km) {
//            this.km = km;
//        }
//
//        public Stream<Map.Entry<String, T>> getStream() {
//            return km.getStream();
//        }
//
//        public Map<String, T> getData() {
//            return km.store;
//        }
//
//        public void remove(String id) {
//            km.store.remove(id);
//        }
//
//        public T copy(T base) {
//            return km.copy(base);
//        }
//    }

}
