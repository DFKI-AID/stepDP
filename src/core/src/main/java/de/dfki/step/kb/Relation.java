package de.dfki.step.kb;

/**
 * Can be used to of n-to-m relations between objects.
 */
public final class Relation {
    private Relation() {

    }

    public static Entity create(String subj, String pred, String obj) {
        Entity e = new Entity();
        e.set(subject, subj);
        e.set(predicate, pred);
        e.set(object, obj);
        return e;
    }


    public static final Attribute<String> subject = new Attribute<>("tocalog/subject");
    public static final Attribute<String> predicate = new Attribute<>("tocalog/predicate");
    public static final Attribute<String> object = new Attribute<>("tocalog/object");

}
