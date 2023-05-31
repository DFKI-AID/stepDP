package de.dfki.step.web.webchat;

import de.dfki.step.web.webchat.server.WebConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Session {
    private String id;
    private List<Message> messages;
    private List<WebConnection> connections;

    public Session (String id) {
        this.messages = new ArrayList<>();
        this.connections = new ArrayList<>();
        this.id = id;
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
                } catch (Exception e) {
                    //remove connections that are dead and check them before trying
                    Socket socket = connection.clientSocket;
                    if (!socket.isConnected()) {
                        try {
                            socket.close();
                            this.connections.remove(connection);
                        } catch (IOException ex) {
                        }
                    }
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

    public String getID (){
        return this.id;
    }
}
