package de.dfki.tocalog.kb;

/**
 */
public class Reference {
    public final String id;
    public final String type;

    public Reference(String id, String type) {
        this.id = id;
        this.type = type;

        if (id == null) {
            throw new IllegalArgumentException("id can't be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("type can't be null");
        }
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public static final Reference None = new Reference("", "");

    public boolean matchesId(String id) {
        return this.id.equals(id);
    }

    public boolean matchesType(String type) {
        return this.type.equals(type);
    }
}
