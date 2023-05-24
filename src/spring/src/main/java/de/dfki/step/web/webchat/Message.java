package de.dfki.step.web.webchat;

public class Message {
    public String sender;
    public String text;

    public Message (String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }
}
