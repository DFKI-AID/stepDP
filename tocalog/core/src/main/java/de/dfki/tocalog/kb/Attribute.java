package de.dfki.tocalog.kb;

import org.pcollections.PMap;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * TODO short name for pretty printing
 */
public class Attribute<T> {
    public final String name;

    public Attribute(String name) {
        this.name = name;
    }

    public boolean matches(Entity entity, Predicate<T> pred) {
        Optional<T> opt = get(entity);
        if (!opt.isPresent()) {
            return false;
        }
        return pred.test(opt.get());
    }

    public T getOrElse(Entity entity, T dflt) {
        Optional<T> opt = get(entity);
        if (opt.isPresent()) {
            return opt.get();
        }
        return dflt;
    }

    public Optional<T> get(Entity entity) {
        AttributeValue attr = entity.attributes.get(name);
        if (attr == null) {
            return Optional.empty();
        }
        T value = (T) attr.value;
        return Optional.ofNullable(value);
    }

    public Entity set(Entity entity, T value) {
        PMap<String, AttributeValue> attributes = entity.attributes.plus(name, new AttributeValue(name, value, this));
        Entity e = new Entity(attributes);
        return e;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute<?> attribute = (Attribute<?>) o;
        return Objects.equals(name, attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                '}';
    }
}
