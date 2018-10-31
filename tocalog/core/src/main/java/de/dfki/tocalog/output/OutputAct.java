package de.dfki.tocalog.output;


import de.dfki.tocalog.kb.Entity;

/**
 */
public class OutputAct<O extends Output> {
    private O output;
    private Entity session;

    public OutputAct(O output, Entity session) {
        this.output = output;
        this.session = session;
    }

    public O getOutput() {
        return output;
    }

    public Entity getSession() {
        return session;
    }
}
