package de.dfki.sc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class SCMain {
    public static void main(String[] args) throws IOException, URISyntaxException {
        URL resource = SCMain.class.getResource("/sc/task_behavior.scxml");
        StateChart sc = Parser.loadStateChart(resource);
        SCEngine engine = new SCEngine(sc);
        AtomicInteger counter = new AtomicInteger(0);
        engine.addCondition("cond1", () -> counter.getAndIncrement() % 2 == 0);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String event = br.readLine();
            engine.fire(event);
        }
    }
}
