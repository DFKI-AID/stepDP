package de.dfki.step.web.webchat.server;

import java.io.*;
import java.net.*;

public class WebServer {
    public WebServer() throws IOException  {
        Thread _thread = new Thread() {
            public void run() {
                ServerSocket _serverSocket = null;
                while (true) {
                    try {
                        if(_serverSocket == null) {
                            _serverSocket = new ServerSocket(11001);
                            System.out.println("Server started.");
                        }

                        Socket clientSocket = null;
                        clientSocket = _serverSocket.accept();
                        System.out.println("Client connected.");

                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        Thread thread = new Thread(clientHandler);
                        thread.start();
                    } catch (IOException e) {
                        System.out.println(e.toString());
                        _serverSocket = null;
                    }
                }
            }
        };
        _thread.start();
    }
}