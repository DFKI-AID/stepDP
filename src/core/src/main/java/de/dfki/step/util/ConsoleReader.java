package de.dfki.step.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * Read text input from the console. Could be used to simulate input.
 */
public class ConsoleReader {
    private Thread consoleReadThread;
    private String input;
    private final Consumer<String> callback;

    public ConsoleReader(Consumer<String> callback) {
        this.callback = callback;
    }

    public synchronized void start() {
        if (consoleReadThread != null) {
            throw new IllegalStateException("ConsoleReader can onyl be started once");
        }

        consoleReadThread = new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String txt = br.readLine();
                    callback.accept(txt);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        consoleReadThread.setDaemon(true);
        consoleReadThread.start();
    }
}
