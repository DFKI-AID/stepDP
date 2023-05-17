package de.dfki.step.web.webchat;

import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.Board;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.web.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebChat {
    Map<Integer, Session> sessions;
    Board blackboard;
    KnowledgeBase kb;

    public WebChat (Board blackboard, KnowledgeBase kb) {
        sessions = new HashMap<>();
        this.blackboard = blackboard;
        this.kb = kb;
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

    public void addMessage (int sessionID, Sender sender, String text) throws IllegalArgumentException{
        if (sessions.get(sessionID)!=null){
            sessions.get(sessionID).addMessage(sender, text);
        }
        else {
            throw new IllegalArgumentException("sessionID is invalid");
        }
    }

    public void addUserMessage (int sessionID, String text) {
        addMessage(sessionID, Sender.USER, text);

        //Create a Token and add to blackboard
        BasicToken token = new BasicToken(kb);
        token.setType(Controller.webChatInputType);
        Map<String,Object> values = new HashMap<>();
        values.put("userText", text);
        values.put("session", sessionID);
        token.addAll(values);
        blackboard.addToken(token);
    }


    public List<Message> getDiscourse (int sessionID) {
        return this.sessions.get(sessionID).getDiscourse();
    }

    public List<Message> getDiscourse (int sessionID, int numberOfMessages) {
        return this.sessions.get(sessionID).getDiscourse(numberOfMessages);
    }
}
