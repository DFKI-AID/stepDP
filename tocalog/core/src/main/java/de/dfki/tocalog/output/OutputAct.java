package de.dfki.tocalog.output;


import de.dfki.tocalog.core.Ontology;

/**
 */
public class OutputAct<O extends Output> {
    private O output;
    private Ontology.Ent session;

    public OutputAct(O output, Ontology.Ent session) {
        this.output = output;
        this.session = session;
    }

    public O getOutput() {
        return output;
    }

    public Ontology.Ent getSession() {
        return session;
    }
}
