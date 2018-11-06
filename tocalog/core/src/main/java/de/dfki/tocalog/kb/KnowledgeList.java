package de.dfki.tocalog.kb;

import org.pcollections.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 */
public class KnowledgeList {
    private PVector<Entity> entities = TreePVector.empty();

    public synchronized void add(Entity entity) {
        this.entities = this.entities.plus(entity);
    }

    public Collection<Entity> getAll() {
        return this.entities;
    }

    public Collection<Entity> query(Predicate<Entity> predicate) {
        Collection<Entity> queryResult = this.entities.stream()
                .filter(e -> predicate.test(e))
                .collect(Collectors.toList());
        return queryResult;
    }

    public synchronized void removeIf(Predicate<Entity> predicate) {
        PVector<Entity> newEntities = entities;
        for (Entity entity : newEntities) {
            if (predicate.test(entity)) {
                newEntities = newEntities.minus(entity);
            }
        }
        this.entities = newEntities;
    }


    public void removeOld(long timeout) {
        long now = System.currentTimeMillis();
        removeIf(ent -> ent.get(Ontology.timestamp).orElse(0l) + timeout < now);
    }


    public void consume(Consumer<Entity> consumer) {
        entities.stream().forEach(consumer);
    }


    public synchronized void update(Function<Entity, Entity> updateFnc) {
        for (Entity entity : entities) {
            Entity newEntity = updateFnc.apply(entity);
            if (entity == newEntity) {
                //no update
                continue;
            }
            this.entities = this.entities.minus(entity);
            this.entities = this.entities.plus(newEntity);
        }
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
}
