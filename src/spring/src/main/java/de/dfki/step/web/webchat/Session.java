package de.dfki.step.web.webchat;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
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

    public int getId () {
        return this.id;
    }

    public List<Message> getDiscourse () {
        return this.messages;
    }

    public List<Message> getDiscourse (int numberOfSentences) {
        List<Message> messages = this.messages.subList(this.messages.size()-Math.min(this.messages.size(),numberOfSentences), this.messages.size());
        return messages;
    }
}
