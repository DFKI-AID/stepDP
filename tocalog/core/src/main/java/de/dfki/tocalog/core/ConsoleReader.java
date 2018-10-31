package de.dfki.tocalog.core;

import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 */
public class ConsoleReader implements EventProducer, InputComponent {
    private Thread consoleReadThread;
    private String input;


    public synchronized void start() {
        if (consoleReadThread != null) {
            throw new IllegalStateException("ConsoleReader can onyl be started once");
        }

        consoleReadThread = new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String txt = br.readLine();
                    ConsoleReader.this.setInput(txt);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        consoleReadThread.setDaemon(true);
        consoleReadThread.start();
    }


    @Override
    public List<Input> process(Event event) {
        Optional<String> optStr = event.tryGet(String.class);
        if (!optStr.isPresent()) {
            return Collections.emptyList();
        }

        TextInput ti = new TextInput(optStr.get());
        return List.of(ti);
    }


    protected synchronized void setInput(String input) {
        this.input = input;
    }

    public synchronized Optional<String> getInput() {
        String i = this.input;
        this.input = null;
        return Optional.ofNullable(i);
    }

    @Override
    public Optional<Event> nextEvent() {
        Optional<String> optTxt = getInput();
        if (!optTxt.isPresent()) {
            return Optional.empty();
        }


        Event event = Event.create(optTxt.get())
                .setSource(this.getClass().getCanonicalName())
                .build();
        return Optional.of(event);
    }
}
