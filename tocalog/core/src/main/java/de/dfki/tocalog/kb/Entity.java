package de.dfki.tocalog.kb;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * TODO rename Base
 * TODO hash and equals
 */
public class Entity {
    public final PMap<String, AttributeValue> attributes;

    public Entity(PMap<String, AttributeValue> attributes) {
        this.attributes = attributes;
    }

    public Entity() {
        this(HashTreePMap.empty());
    }

    public Entity unset(Attribute attr) {
        PMap<String, AttributeValue> newAttr = attributes.minus(attr.name);
        return new Entity(newAttr);
    }

    public <T> Entity set(Attribute<T> attr, T value) {
        return attr.set(this, value);
    }

    public <T> Optional<T> get(Attribute<T> attr) {
        return attr.get(this);
    }

    public <T> boolean matches(Attribute<T> attr, Predicate<T> pred) {
        return attr.matches(this, pred);
    }

    public <T> Entity plus(Attribute<T> attr, Function<T, T> fnc) {
        Optional<T> optVal = this.get(attr);
        if (!optVal.isPresent()) {
            //TODO maybe of default?
            return this;
        }
        T value = fnc.apply(optVal.get());
        return this.set(attr, value);
    }

    public Entity merge(Entity other) {
        Entity out = this;
        for (AttributeValue av : other.attributes.values()) {
            Object value = av.attribute.get(other).get();
            out = out.set(av.attribute, value);
        }
        return out;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ent{");
        for (AttributeValue av : attributes.values()) {
            sb.append(av.name).append("=");
            sb.append(av.value);
            sb.append(" ");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;

        for(AttributeValue av : attributes.values()) {
            if(!entity.attributes.containsKey(av.name)) {
                return false;
            }

            if(!av.equals(entity.attributes.get(av.name))) {
                return false;
            }
        }
        for(AttributeValue av : entity.attributes.values()) {
            if(!attributes.containsKey(av.name)) {
                return false;
            }

            if(!av.equals(attributes.get(av.name))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        //TODO
        return Objects.hash(attributes.values());
    }

    /**
     * @param attributes
     * @throws IllegalArgumentException if one or more attributes are not present
     */
    public void validate(Attribute... attributes) {
        for (Attribute attr : attributes) {
            if (!get(attr).isPresent()) {
                throw new IllegalArgumentException(String.format("invalid entity. missing %s. %s", attr.name, this));
            }
        }
    }

    public boolean isEmpty() {
        return this.attributes.isEmpty();
    }

    public static final Entity empty = new Entity();
}
