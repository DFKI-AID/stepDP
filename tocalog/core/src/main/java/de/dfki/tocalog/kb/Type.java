package de.dfki.tocalog.kb;

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
}
