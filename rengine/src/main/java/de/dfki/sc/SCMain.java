package de.dfki.sc;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
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
        URL resource = SCMain.class.getResource("/sc/simple.scxml");
        StateChart sc = Parser.parse(resource);
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
