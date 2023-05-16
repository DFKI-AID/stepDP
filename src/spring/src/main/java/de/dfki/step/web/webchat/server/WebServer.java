package de.dfki.step.web.webchat.server;

import java.io.*;
import java.net.*;

public class WebServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(11001);
        System.out.println("Server started.");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected.");

            ClientHandler clientHandler = new ClientHandler(clientSocket);
            Thread thread = new Thread(clientHandler);
            thread.start();
        }
    }
}