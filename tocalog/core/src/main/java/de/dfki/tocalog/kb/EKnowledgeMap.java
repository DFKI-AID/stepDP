package de.dfki.tocalog.kb;

import de.dfki.tocalog.model.Entity;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 */
public class EKnowledgeMap<T extends Entity> extends KnowledgeMap<T> {
    public void add(T entity) {
        entity.setTimestamp(System.currentTimeMillis());
        if(!entity.getId().isPresent()) {
            throw new IllegalArgumentException("can't store entity without id in knowledge base. enitity was " + entity);
        }
        super.put(entity.getId().get(), entity);
    }

    public void removeOld(long timeout) {
        try {
            long now = System.currentTimeMillis();
            KnowledgeMap.Locked<T> l = this.lock();
            l.getData().entrySet().removeIf(e -> e.getValue().getTimestamp().orElse(0l) + timeout < now);
        } finally {
            this.unlock();
        }
    }

    public void updateTimestamp(String id) {
        try {
            long now = System.currentTimeMillis();
            KnowledgeMap.Locked<T> l = this.lock();
            l.getData().get(id).setTimestamp(now);
        } finally {
            this.unlock();
        }
    }

    //TODO other helper functions
}
