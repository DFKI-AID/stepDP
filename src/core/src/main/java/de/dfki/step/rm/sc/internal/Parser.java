package de.dfki.step.rm.sc.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * TODO not all scxml features are supported. e.g. nested states may produce problems
 */
public class Parser {
    private static final Logger log = LoggerFactory.getLogger(Parser.class);

    public static StateChart loadStateChart(InputStream inputStream) throws URISyntaxException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            return loadStateChart(doc);
        } catch (Exception e1) {
            throw new IOException(e1);
        }
    }

    public static StateChart loadStateChart(URL resource) throws URISyntaxException, IOException {
        File file = new File(resource.toURI());
        return loadStateChart(file);
    }


    public static StateChart loadStateChart(File file) throws URISyntaxException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            return loadStateChart(doc);
        } catch (Exception e1) {
            throw new IOException(e1);
        }
    }

    public static StateChart loadStateChart(Document doc) throws IOException {
        try {
            doc.getDocumentElement().normalize();
            log.debug("Root element: {}", doc.getDocumentElement().getNodeName());
            Node scxmlNode = doc.getElementsByTagName("scxml").item(0);

            State state = parseState(scxmlNode);

            String initialState = state.getInitial();
            if (!state.hasInitial()) {
                throw new IllegalArgumentException("Missing initial state for root");
            }


            StateChart sc = new StateChart();
            sc.setRoot(state);
            sc.setInitialState(initialState);
            return sc;
        } catch (Exception e1) {
            throw new IOException(e1);
        }
    }

    public static Map<String, Set<String>> loadRuleActivationMap(InputStream stream) throws IOException {
        return loadRuleActivationMap(new InputStreamReader(stream));
    }

    public static Map<String, Set<String>> loadRuleActivationMap(File file) throws IOException {
        return loadRuleActivationMap(new FileReader(file));
    }

    public static Map<String, Set<String>> loadRuleActivationMap(Reader reader) throws IOException {
        Map<String, Set<String>> result = new HashMap<>();
        try {
            String cvsSplitBy = ",";
            BufferedReader br = new BufferedReader(reader);
            String line;

            line = br.readLine();
            if (line == null) {
                throw new IOException("Can't read rule activation file: Missing header");
            }

            String[] headerSplit = line.split(cvsSplitBy);
            Map<Integer, String> rules = new HashMap<>();
            for (int i = 1; i < headerSplit.length; i++) {
                rules.put(i, headerSplit[i]);
            }

            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] lineSplit = line.split(cvsSplitBy);
                String state = lineSplit[0];
                result.put(state, new HashSet<>());

                for (int i = 1; i < lineSplit.length; i++) {
                    String ruleName = rules.get(i);
                    if (ruleName == null) {
                        //empty column
                        continue;
                    }
                    String value = lineSplit[i];
                    if (Objects.equals(value, "TRUE")) {
                        result.get(state).add(ruleName);
                    }
                }
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static State parseState(Node node) throws IOException {
        Element element = (Element) node;
        String id = getAttrValue("id", element);
        State state = new State(id);

        //find initial state
        //the initial state can also be encoded as a child node
        String initialState = getAttrValue("initial", element);
        state.setInitial(initialState);

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (Objects.equals(child.getNodeName(), "state")) {
                State childState = parseState(childNodes.item(i));
                state.addChildState(childState);
            }
            if (Objects.equals(child.getNodeName(), "transition")) {
                Transition transition = parseTransition(childNodes.item(i));
                if(transition.getTarget().isEmpty()) {
                    //if the target is not set, it is a transition to itself
                    transition.setTarget(id);
                }
                state.addTransition(transition);
            }

            if (Objects.equals(child.getNodeName(), "onentry")) {
                OnEntry onEntry = parseOnEntry(childNodes.item(i));
                state.addOnEntry(onEntry);
            }

            if (Objects.equals(child.getNodeName(), "onexit")) {
                OnExit onExit = parseOnExit(childNodes.item(i));
                state.addOnExit(onExit);
            }

            if (Objects.equals(child.getNodeName(), "qt:editorinfo")) {
                Optional<Geometry> geometry = parseGeometry(childNodes.item(i));
                if (geometry.isPresent()) {
                    state.setGeometry(geometry.get());
                }
            }

            if (Objects.equals(child.getNodeName(), "initial")) {
                if (initialState == null) {
                    throw new IllegalStateException("the initial state should be only defined once per state");
                }
                initialState = ((Element) child.getFirstChild()).getAttribute("target");
                state.setInitial(initialState);
            }
        }
        return state;
    }

    private static OnExit parseOnExit(Node item) {
        //OnExit has the same structure as OnEntry
        OnEntry onEntry = parseOnEntry(item);
        OnExit onExit = new OnExit();
        onEntry.getScripts().forEach(s -> onExit.addScript(s));
        return onExit;
    }

    private static Optional<Geometry> parseGeometry(Node item) {
        Element element = (Element) item;
        String geo = element.getAttribute("geometry");
        if (geo.isEmpty()) {
            return Optional.empty();
        }
        Geometry geometry = new Geometry(geo);
        return Optional.of(geometry);
    }

    private static OnEntry parseOnEntry(Node node) {
        OnEntry onEntry = new OnEntry();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (!(child instanceof Element)) {
                continue;
            }
            Element element = (Element) child;
            if (!Objects.equals(element.getTagName(), "script")) {
                continue;
            }
            String scriptSrc = element.getAttribute("src");
            onEntry.addScript(scriptSrc);
        }
        return onEntry;
    }

    public static Transition parseTransition(Node node) throws IOException {
        Element element = (Element) node;
        Transition transition = new Transition();
        transition.setEvent(element.getAttribute("event"));
        transition.setTarget(element.getAttribute("target"));
        transition.setCond(element.getAttribute("cond"));

        String scriptSrc = element.getAttribute("src");
        if (scriptSrc != null && !scriptSrc.isEmpty()) {
            transition.addScript(scriptSrc);
        }

        // find script tags that are executed if a transition fires
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (!childNode.getNodeName().equals("script")) {
                log.warn("Ignoring unsupported node under transition {}", childNode.getNodeName());
                continue;
            }

            if (!(childNode instanceof Element)) {
                throw new IOException("Invalid file: Expected node to be an Element: structure transition -> script");
            }

            scriptSrc = ((Element) childNode).getAttribute("src");
            transition.addScript(scriptSrc);
        }

        return transition;
    }

    private static String getAttrValue(String tag, Element element) {
        return element.getAttribute(tag);
    }


}
