package de.dfki.step.web.webchat.server;
import de.dfki.step.web.Controller;
import de.dfki.step.web.webchat.Message;

import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    Socket clientSocket;
    public ClientHandler(Socket socket) {

        this.clientSocket = socket;
    }

    public void run() {

        WebConnection cConnection;
        while (true) {
            try {
                cConnection = new WebConnection(this.clientSocket);
                String sessionID = cConnection.receiveMessage();
                Controller.webChat.getSession(sessionID).addConnection(cConnection);
                String inputLine;
                while ((inputLine = cConnection.receiveMessage()) != null) {
                    if (inputLine.contains("getDiscourse")){
                        List<Message> discourses = Controller.webChat.getDiscourse(sessionID);
                        if (discourses.toArray().length > 0) {

                            String message = "";
                            for (Message discourse : discourses) {
                                message = discourse.sender + ":" + discourse.text + "\n";
                                cConnection.sendMessage(message);
                            }
                        }
                    }
                    else{

                        if (this.clientSocket.isConnected() && inputLine.charAt(0) != '\u0003'){
                            Controller.webChat.currentConnection = cConnection;
                            Controller.webChat.addUserMessage(sessionID, inputLine, false);
                        }
                }
                System.out.println("Client Disconnected");
                }
            }
            catch (Exception e)
            {

            }
            finally{
            }
        }

    }

}
