package de.dfki.rengine;

/**
 *
 */
public class Implication {
    private final Token data;
    private final Runnable implication;

    public Implication(Token data, Runnable implication) {
        this.data = data;
        this.implication = implication;
    }
}
