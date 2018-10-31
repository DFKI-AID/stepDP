package de.dfki.tocalog.dialog.sc;

/**
 */
public abstract class State {
    private final String id;

    public State(String id) {
        this.id = id;
    }


    public String getId() {
        return id;
    }
}
