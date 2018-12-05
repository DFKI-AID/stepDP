package de.dfki.tocalog.kb;

import java.util.Objects;

/**
 */
public class AttributeValue<T> {
    /**
     * field type
     */
    public final Attribute<T> attribute;
    /**
     * field name
     */
    public final String name;
    /**
     * current value
     */
    public final T value;

    public AttributeValue(String name, T value, Attribute<T> attribute) {
        this.name = name;
        this.value = value;
        this.attribute = attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeValue<?> that = (AttributeValue<?>) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value) &&
                Objects.equals(attribute, that.attribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, attribute);
    }
}
