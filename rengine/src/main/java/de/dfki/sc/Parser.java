package de.dfki.sc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 *
 */
public class Parser {


    public static StateChart parse(URL resource) throws URISyntaxException, IOException {
        File file = new File(resource.toURI());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            Node scxmlNode = doc.getElementsByTagName("scxml").item(0);

            State state = parseState(scxmlNode);

            String initialState = ((Element) scxmlNode).getAttribute("initial");
            //TODO not available, state missing

            StateChart sc = new StateChart();
            sc.setRoot(state);
            sc.setInitialState(initialState);
            return sc;
        } catch (Exception e1) {
            throw new IOException(e1);
        }
    }


    public static State parseState(Node node) {
        Element element = (Element) node;
        String id = getAttrValue("id", element);
        State state = new State(id);

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (Objects.equals(child.getNodeName(), "state")) {
                State childState = parseState(childNodes.item(i));
                state.addChildState(childState);
            }
            if (Objects.equals(child.getNodeName(), "transition")) {
                Transition transition = parseTransition(childNodes.item(i));
                state.addTransition(transition);
            }
        }
        return state;
    }

    public static Transition parseTransition(Node node) {
        Element element = (Element) node;
        Transition transition = new Transition();
        transition.setEvent(element.getAttribute("event"));
        transition.setTarget(element.getAttribute("target"));
        transition.setCond(element.getAttribute("cond"));
        return transition;
    }

    private static String getAttrValue(String tag, Element element) {
        return element.getAttribute(tag);
    }
}
