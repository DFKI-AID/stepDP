package de.dfki.step.web.webchat;

import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.Board;
import de.dfki.step.kb.KnowledgeBase;
import de.dfki.step.web.Controller;
import de.dfki.step.web.webchat.server.WebConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebChat {
    Map<String, Session> sessions;
    Board blackboard;
    KnowledgeBase kb;

    public WebConnection currentConnection;

    public WebChat (Board blackboard, KnowledgeBase kb) {
        this.sessions = new HashMap<>();
        this.blackboard = blackboard;
        this.kb = kb;
    }
    public Session getSession(String session)
    {
        if (this.sessions.get(session) == null){
            Session sess = new Session();
            this.sessions.put(session, sess);
            return sess;
        }
        return this.sessions.get(session);

    }

    public void addMessage (String sessionID, String sender, String text, Boolean sendMessage) {
        if (this.sessions.get(sessionID) == null){
            Session sess = new Session();
        this.sessions.put(sessionID, sess);
        }
        this.sessions.get(sessionID).addMessage(sender, text);
        this.sessions.get(sessionID).sendMessage(text, sender, this.currentConnection, sendMessage);
    }

    public void addUserMessage (String sessionID, String text, Boolean sendMessage) {
        System.out.println(text);
        this.addMessage(sessionID, "user", text, sendMessage);

        //dialogue isnt adding messages for bot, when that is fixed remove the line below
        //this.addMessage(sessionID, "bot", text);


        //Create a Token and add to blackboard
        BasicToken token = new BasicToken(kb);
        token.setType(Controller.webChatInputType);
        Map<String,Object> values = new HashMap<>();
        values.put("userText", text);
        values.put("session", sessionID);
        token.addAll(values);
        this.blackboard.addToken(token);
    }


    public List<Message> getDiscourse (String sessionID) {

        if (this.sessions.containsKey(sessionID))
        {
            return this.sessions.get(sessionID).getDiscourse();
        }
        return null;

    }

    public List<Message> getDiscourse (String sessionID, int numberOfMessages) {
        return this.sessions.get(sessionID).getDiscourse(numberOfMessages);
    }
}
