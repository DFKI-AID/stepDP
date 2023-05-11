package de.dfki.step.web.webchat;

import java.util.HashMap;
import java.util.List;
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

    public void addMessage (int sessionID, Sender sender, String text) {
        sessions.get(sessionID).addMessage(sender, text);
    }


    public List<Message> getDiscourse (int sessionID) {
        return this.sessions.get(sessionID).getDiscourse();
    }

    public List<Message> getDiscourse (int sessionID, int numberOfMessages) {
        return this.sessions.get(sessionID).getDiscourse(numberOfMessages);
    }
}
