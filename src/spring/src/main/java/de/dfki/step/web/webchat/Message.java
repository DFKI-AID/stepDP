package de.dfki.step.web.webchat;

public class Message {
    private Sender sender;
    private String text;

    public Message (Sender sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public Sender getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }
}
