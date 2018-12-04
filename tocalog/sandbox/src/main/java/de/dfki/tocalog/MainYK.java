package de.dfki.tocalog;

import de.dfki.tocalog.core.*;
import de.dfki.tocalog.dialog.sc.*;
import de.dfki.tocalog.kb.Entity;
import de.dfki.tocalog.kb.KnowledgeMap;
import de.dfki.tocalog.kb.Ontology;
import de.dfki.tocalog.output.OutputFactory;
import de.dfki.tocalog.output.impp.*;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaHypoProducer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
                .set(Ontology.service, "console");
        services.add(consoleService);

        OutputFactory of = new OutputFactory();
        Entity output1 = of.createTTSOutput("hello world");
        Entity output2 = of.createTextOutput("hello world 2");
        Entity output3 = of.createTextOutput("how are you?");
        Entity output4 = of.createTTSOutput("how are you?");

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

        StateChart.Callback<Hypothesis> callback = new StateChart.Callback<Hypothesis>() {
            @Override
            public void onFire(StateChart.FireEvent<Hypothesis> event) {
                System.out.println("ok: " + event.event);
            }
        };

        StateChart<Hypothesis> sc = new StateChart.Builder<Hypothesis>()
                .setInitialState("A")
                .addTransition("A", "B", (h -> {
                    if (!h.getIntent().equals("turnOn")) {
                        return false;
                    }
                    Collection<Entity> devices = h.getSlot("device").orElse(Slot.Empty).getCandidates();
                    Set<Entity> fans = devices.stream()
                            .filter(d -> d.get(Ontology.name).orElse("").equals("fan"))
                            .collect(Collectors.toSet());
                    if (fans.isEmpty()) {
                        return false;
                    }
                    return true;
                }))
                .addTransition("B", "A", (h -> h.getIntent().equals("turnOff")))
                .setCallback(callback)
                .build();


        StateChart<Hypothesis> scGreeting = new StateChart.Builder<Hypothesis>()
                .setInitialState("Greeting")
                .addTransition("Greeting", "Greeting", (h -> {
                    if (!h.getIntent().equals("greeting")) {
                        return false;
                    }
                    return true;
                }))
                .build();

        HypothesisProducer rasaHp = new RasaHypoProducer(new RasaHelper(new URL("http://localhost:5000/parse")));
        DialogComponent dc = new SCDialogComponent(sc, rasaHp);

        DialogApp app = DialogApp.create()
                .addEventProducer(consoleReader)
                .addInputComponent(consoleReader)
                .addDialogComponent(dc)
                .addDialogComponent(new SCDialogComponent(scGreeting, rasaHp))
                .build();

        app.run();
//        String mmGraph = sc.toMermaid();
//        FileUtils.writeStringToFile(new File("~/tmp/graph.txt"), mmGraph, Charset.defaultCharset());
    }


}
