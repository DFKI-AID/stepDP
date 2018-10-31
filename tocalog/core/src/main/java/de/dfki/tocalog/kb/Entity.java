package de.dfki.tocalog.kb;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.Optional;
import java.util.function.Function;

/**
 * TODO rename Base
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
            sb.append("  ");
        }
        sb.append("}");
        return sb.toString();
    }
}
