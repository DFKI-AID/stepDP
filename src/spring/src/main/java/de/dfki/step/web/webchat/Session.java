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

    /**
     * adds a connection to the list of connections that messages are sent to
     * @param ccon the connection that is supposed to be added
     */
    public void addConnection(WebConnection ccon)
    {
        connections.add(ccon);
    }

    /**
     * sends a message to all connections from the list and removes closed connections
     * @param message the message that is supposed to be sent
     * @param sender who does the message come from? ("bot" or "user")
     * @param currentConnection connection that the message comes from
     * @param sendMessage Boolean that specifies, if the message is supposed to be sent to the webSocket
     */
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

    /**
     * saves a message to the discourse
     * @param sender who does the message come from? ("bot" or "user")
     * @param text text to be saved
     */
    public void addMessage (String sender, String text) {
        Message message = new Message(sender, text);
        messages.add(message);
    }

    public List<Message> getDiscourse () {
        return this.messages;
    }

    /**
     * retrieves the last messages from the discourse
     * @param numberOfSentences the number of messages to be returned
     * @return list of messages (including sender and text)
     */
    public List<Message> getDiscourse (int numberOfSentences) {
        List<Message> messages = this.messages.subList(this.messages.size()-Math.min(this.messages.size(),numberOfSentences), this.messages.size());
        return messages;
    }

    public String getID (){
        return this.id;
    }
}
