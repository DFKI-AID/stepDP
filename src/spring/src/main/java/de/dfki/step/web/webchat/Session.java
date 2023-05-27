package de.dfki.step.web.webchat;

import com.google.gson.Gson;
import de.dfki.step.web.webchat.server.WebConnection;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Session {
    int id;
    List<Message> messages;
    List<WebConnection> connections;

    public Session () {
        this.messages = new ArrayList<>();
        connections = new ArrayList<>();
    }
    public void addConnection(WebConnection ccon)
    {
        connections.add(ccon);
    }
    public void sendMessage(String message, String sender, WebConnection currentConnection,  Boolean sendMessage)
    {
        for (WebConnection connection: this.connections) {
            if (connection!=currentConnection || sendMessage == true) {
                try {
                    connection.sendMessage(sender + ":" + message);
                } catch (Exception E) {
                    // remove connections that are dead and check them before trying
                }
            }
        }
    }

    public void addMessage (String sender, String text) {
        Message message = new Message(sender, text);
        messages.add(message);
    }

    public List<Message> getDiscourse () {
        return this.messages;
    }

    public List<Message> getDiscourse (int numberOfSentences) {
        List<Message> messages = this.messages.subList(this.messages.size()-Math.min(this.messages.size(),numberOfSentences), this.messages.size());
        return messages;
    }
}
