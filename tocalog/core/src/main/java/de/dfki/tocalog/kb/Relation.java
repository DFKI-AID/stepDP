package de.dfki.tocalog.kb;

import de.dfki.tocalog.core.Ontology;

/**
 * Can be used to create n-to-m relations between objects.
 */
public final class Relation {
    private Relation() {

    }

    public static Ontology.Ent create(String subj, String pred, String obj) {
        Ontology.Ent e = new Ontology.Ent();
        e.set(subject, subj);
        e.set(predicate, pred);
        e.set(object, obj);
        return e;
    }


    public static final Ontology.Attribute<String> subject = new Ontology.Attribute<>("tocalog/subject");
    public static final Ontology.Attribute<String> predicate = new Ontology.Attribute<>("tocalog/predicate");
    public static final Ontology.Attribute<String> object = new Ontology.Attribute<>("tocalog/object");

}
