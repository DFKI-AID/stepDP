package de.dfki.step.web.webchat;

public class Message {
    Sender sender;
    String text;

    public Message (Sender sender, String text) {
        this.sender = sender;
        this.text = text;
    }
}
