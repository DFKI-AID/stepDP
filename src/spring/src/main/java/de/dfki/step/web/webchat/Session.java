package de.dfki.step.web.webchat;

import java.util.ArrayList;
import java.util.List;

public class Session {
    int id;
    List<Message> messages;

    public Session (int id) {
        this.id = id;
        this.messages = new ArrayList<>();
    }

    public void addMessage (Sender sender, String text) {
        Message message = new Message(sender, text);
        messages.add(message);
    }
}
