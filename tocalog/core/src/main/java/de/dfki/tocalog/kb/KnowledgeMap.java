package de.dfki.tocalog.kb;

import de.dfki.sire.Base;
import de.dfki.sire.CborDeserializer;
import de.dfki.sire.CborSerializer;
import de.dfki.tocalog.core.Ontology;

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
public class KnowledgeMap {
    private CborSerializer serializer = new CborSerializer();
    private CborDeserializer deserializer = new CborDeserializer();
    private Lock lock = new ReentrantLock();
    private Random rdm = new Random();

    private Map<String, Ontology.Ent> entities = new HashMap<>();

    public synchronized void add(Ontology.Ent ent) {
        Optional<String> id = ent.get(Ontology.id);
        if (!id.isPresent()) {
            throw new IllegalArgumentException("need id for putting entity into kb");
        }
        this.entities.put(id.get(), ent);
    }

    public synchronized String add(Ontology.Ent ent, Ontology.Attribute... attributes) {
        Optional<String> optId = ent.get(Ontology.id);
        String id = optId.orElse(UUID.randomUUID().toString());

        Ontology.Ent kbEnt;

        if (this.entities.containsKey(id)) {
            kbEnt = this.entities.get(id);
        } else {
            kbEnt = new Ontology.Ent();
            kbEnt.set(Ontology.id, id);
        }

        for (Ontology.Attribute attr : attributes) {
            Optional optValue = ent.get(attr);
            if (!optValue.isPresent()) {
                throw new IllegalStateException("value not presented for " + attr);
            }
            kbEnt = kbEnt.set(attr, optValue.get());
        }

        this.entities.put(optId.get(), kbEnt);
        return id;
    }

    public synchronized <T> void update(String id, Ontology.Attribute<T> attr, T value) {
        if (!this.entities.containsKey(id)) {
            //TODO could also create a new entity
            return;
        }
        Ontology.Ent ent = this.entities.get(id);
        ent = ent.set(attr, value);
        this.entities.put(id, ent);
    }


    //TODO optional or empty ent?
    public synchronized Optional<Ontology.Ent> get(String id) {
        return Optional.ofNullable(this.entities.get(id));
    }


    public synchronized Collection<Ontology.Ent> query(Predicate<Ontology.Ent> predicate) {

    }

    public synchronized boolean removeif(Predicate<Ontology.Ent> predicate) {
        return entities.entrySet().removeIf(e -> predicate.test(e.getValue()));
    }


    public synchronized void removeOld(long timeout) {
        long now = System.currentTimeMillis();
        removeif(ent -> ent.get(Ontology.timestamp).orElse(0l) + timeout < now);
    }


    public synchronized void apply(Consumer<Ontology.Ent> consumer) {
        entities.values().stream().forEach(consumer);
    }


    public synchronized void updateTimestamp(Predicate<Ontology.Ent> pred) {
        long now = System.currentTimeMillis();
        entities.entrySet().stream()
                .filter(e -> pred.test(e.getValue()))
                .forEach(e -> e.getValue().set(Ontology.timestamp, now));

    }


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
