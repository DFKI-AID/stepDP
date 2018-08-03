package de.dfki.tocalog.output;

import de.dfki.tocalog.model.Session;

/**
 */
public class OutputAct<O extends Output> {
    private O output;
    private Session session;

    public OutputAct(O output, Session session) {
        this.output = output;
        this.session = session;
    }

    public O getOutput() {
        return output;
    }

    public Session getSession() {
        return session;
    }
}
