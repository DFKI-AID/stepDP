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

    /**
     * get an existing or new session with the given ID
     * @param sessionID ID that the session has or should be given
     * @return the retrieved or newly cerated session
     */
    public Session getSession(String sessionID)
    {
        Session session = this.sessions.get(sessionID);
        if (session == null){
            session = new Session(sessionID);
            this.sessions.put(sessionID, session);
        }
        return session;

    }

    private void addMessage (String sessionID, String sender, String text, Boolean sendMessage) {
        Session session = this.getSession(sessionID);
        session.addMessage(sender, text);
        session.sendMessage(text, sender, this.currentConnection, sendMessage);
    }

    /**
     * saves a message from the bot in the discourse
     * @param sessionID id of the session (newly created, if it does not exist)
     * @param text text of the message to be saved
     */
    public void addBotMessage (String sessionID, String text) {
        this.addMessage(sessionID, "bot", text, true);
    }

    /**
     * saves a message as user message and creates a Token to generate the bot response
     * @param sessionID id of the session (newly created, if it does not exist)
     * @param text text of the message to be saved
     * @param sendMessage Boolean that specifies, if the message is supposed to be sent to the webSocket
     */
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

    /**
     * retrieves the entire discourse from the session
     * @param sessionID id of the session (newly created, if it does not exist)
     * @return entire discourse as a List of messages (sender and text)
     */
    public List<Message> getDiscourse (String sessionID) {
        Session session;
        if (this.sessions.containsKey(sessionID))
        {
            session = this.sessions.get(sessionID);
        } else {
            session = new Session(sessionID);
            sessions.put(sessionID, session);
        }
        return session.getDiscourse();
    }

    /**
     * retrieves the latest discourse from the session
     * @param sessionID id of the session (newly created, if it does not exist)
     * @param numberOfMessages the number of messages that should be returned (maximum)
     * @return List of messages (sender and text)
     */
    public List<Message> getDiscourse (String sessionID, int numberOfMessages) {
        Session session;
        if (this.sessions.containsKey(sessionID))
        {
            session = this.sessions.get(sessionID);
        } else {
            session = new Session(sessionID);
            sessions.put(sessionID, session);
        }
        return session.getDiscourse(numberOfMessages);
    }
}
