package de.dfki.tocalog;

import de.dfki.tocalog.core.*;
import de.dfki.tocalog.dialog.sc.*;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.output.Output;
import de.dfki.tocalog.output.SpeechOutput;
import de.dfki.tocalog.output.TextOutput;
import de.dfki.tocalog.output.impp.*;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaHypoProducer;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 */
public class MainYK {
    public static void main(String[] args) throws IOException, InterruptedException {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
        statechart(args);
//        imp(args);
//        framework(args);
    }

    public static void impp(String[] args) {
        KnowledgeMap services = new KnowledgeMap();
        Entity consoleService = new Entity()
                .set(Ontology.id, "c1")
                .set(Ontology.serviceType, "console");
        services.add(consoleService);

        Output output1 = new SpeechOutput("hello world");
        Output output2 = new TextOutput("hello world 2");
        Output output3 = new TextOutput("how are you?");
        Output output4 = new SpeechOutput("how are you?");

        OutputNode node =
                OutputNode.buildNode(OutputNode.Semantic.concurrent)
                        .addNode(OutputNode.buildNode(OutputNode.Semantic.complementary).setId("abcdef")
                                .addNode(OutputNode.buildNode(output1).addService("s456").build())
                                .addNode(OutputNode.buildNode(output2).addService("s1").build())
                                .build())
                        .addNode(OutputNode.buildNode(OutputNode.Semantic.alternative)
                                .addNode(OutputNode.buildNode(output3)
//                                    .addService("blubb-textview")
                                        .build())
                                .addNode(OutputNode.buildNode(output4)
//                                    .addService("blubb-loudspeaker1")
//                                    .addService("blubb-loudspeaker2")
                                        .build())
                                .build())
                        .build();

        ConsoleAssigner consoleAssigner = new ConsoleAssigner(services);
        consoleAssigner.assignConsoleService(node);
        System.out.println(PrintVisitor.print(node));

        boolean isPresentable = new PresentableVisitor().isPresentable(node);
        System.out.println("can be presented: " + isPresentable);

        System.out.println(PrintVisitor.print(node));


        PresenterVisitor pv = new PresenterVisitor();
        pv.visit(node);
        node = pv.getResult();

        System.out.println(PrintVisitor.print(node));

        OutputNode nodeCopy = new CopyVisitor().copy(node).build();
        System.out.println(PrintVisitor.print(nodeCopy));

        Optional<OutputNode> singleNode = new FindNodeVisitor(n -> n.getId().equals("abcdef")).find(nodeCopy);
        singleNode.ifPresent(n -> System.out.println(PrintVisitor.print(n)));


    }


    public static void statechart(String[] args) throws IOException {

        ConsoleReader consoleReader = new ConsoleReader();
        consoleReader.start();



        StateChart sc = StateChart.create()
                .setInitialState("A")
                .addTransition("A", "turnOn", "B")
                .addTransition("B", "turnOff", "A")
                .build();

        HypothesisProducer rasaHp = new RasaHypoProducer(new RasaHelper(new URL("http://localhost:5000/parse")));
        DialogComponent dc = new SCDialogComponent(sc, rasaHp);

        DialogApp app = DialogApp.create()
                .addEventProducer(consoleReader)
                .addInputComponent(consoleReader)
                .addDialogComponent(dc)
                .build();

        app.run();
//        String mmGraph = sc.toMermaid();
//        FileUtils.writeStringToFile(new File("~/tmp/graph.txt"), mmGraph, Charset.defaultCharset());
    }


}
