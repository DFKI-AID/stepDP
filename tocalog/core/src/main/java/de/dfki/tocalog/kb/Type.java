package de.dfki.tocalog.kb;

import java.util.Objects;

/**
 */
public class Type {
    public final String name;

    public Type(String name) {
        this.name = name;
    }

    public Reference refTo(String id) {
        return new Reference(id, name);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return Objects.equals(name, type.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Type{" +
                "name='" + name + '\'' +
                '}';
    }
}
