package de.dfki.step.deprecated.kb;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Encapsulates multiple {@link KnowledgeMap} for read and write access.
 */
@Deprecated
public class KMView {
    private List<KnowledgeMap> kmaps = new ArrayList<>();

    public KMView(Collection<KnowledgeMap> kmaps) {
        this.kmaps.addAll(kmaps);
    }

    public KMView(KnowledgeMap... kmaps) {
        this(Arrays.asList(kmaps));
    }

    public <T> void update(String id, Attribute<T> attr, T value) {
        kmaps.forEach(
                km -> km.append(id, attr, value)
        );
    }

    public void update(Function<Entity, Entity> updateFnc) {
        kmaps.forEach(
                km -> km.add(updateFnc)
        );
    }

    /**
     * @param id
     * @return The first entity found with the given id
     */
    public Optional<Entity> get(String id) {
        for (KnowledgeMap km : kmaps) {
            Optional<Entity> entity = km.get(id);
            if (entity.isPresent()) {
                return entity;
            }
        }
        return Optional.empty();
    }


    public Collection<Entity> query(Predicate<Entity> predicate) {
        ArrayList<Entity> queryResult = new ArrayList<>();
        kmaps.forEach(
                km -> queryResult.addAll(km.query(predicate))
        );
        return queryResult;
    }

    public void removeIf(Predicate<Entity> predicate) {
        kmaps.forEach(
                km -> km.removeIf(predicate)
        );
    }
}
