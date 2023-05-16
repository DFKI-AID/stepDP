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


                String inputLine;
                while ((inputLine = cConnection.receiveMessage()) != null) {
                    if (inputLine.contains("getDiscourse sessionID:")){
                        int sessionID =  Integer.parseInt(inputLine.split("sessionid:")[1]);
                        List<Message> discourse = Controller.webChat.getDiscourse(sessionID);
                        cConnection.sendMessage(discourse.toString());
                    }

                    cConnection.sendMessage("From Server: " + inputLine);
                }
                System.out.println("Client disconnected");
            }
            catch (Exception e)
            {

            }
            finally{

            }
        }

    }

}