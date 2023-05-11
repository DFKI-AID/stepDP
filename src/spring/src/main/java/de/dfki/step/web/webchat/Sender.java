package de.dfki.step.web.webchat;

public enum Sender {
    USER(0),
    BOT(1);

    private final int index;

    Sender(int index) {
        this.index = index;
    }
}
