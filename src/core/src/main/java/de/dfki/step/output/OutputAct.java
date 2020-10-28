package de.dfki.step.output;


import de.dfki.step.deprecated.kb.Entity;

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
