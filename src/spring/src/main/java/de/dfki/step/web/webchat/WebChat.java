package de.dfki.step.web.webchat;

import java.util.HashMap;
import java.util.Map;

public class WebChat {
    Map<Integer, Session> sessions;

    public WebChat () {
        sessions = new HashMap<>();
    }

    public int addSession () {
        Integer i = 1;
        while (sessions.keySet().contains(i)) {
            i++;
        }
        Session session = new Session(i);
        sessions.put(i, session);
        return i;
    }

    public void sendMessage (int sessionID, String text) {
        sessions.get(sessionID).addMessage(Sender.BOT, text);
    }

    public Message receiveMessage (int sessionID) {
        //TODO: implement
        return null;
    }
}
