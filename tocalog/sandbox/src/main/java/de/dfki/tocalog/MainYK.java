package de.dfki.tocalog;

import de.dfki.tocalog.dialog.Intent;
import de.dfki.tocalog.dialog.MetaDialog;
import de.dfki.tocalog.dialog.sc.State;
import de.dfki.tocalog.dialog.sc.StateChart;
import de.dfki.tocalog.dialog.sc.StateChartEvent;
import de.dfki.tocalog.dialog.sc.Transition;
import de.dfki.tocalog.framework.DialogComponent;
import de.dfki.tocalog.framework.Event;
import de.dfki.tocalog.framework.ProjectManager;
import de.dfki.tocalog.input.Input;
import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.kb.EKnowledgeMap;
import de.dfki.tocalog.model.Person;
import de.dfki.tocalog.model.Service;
import de.dfki.tocalog.model.Triple;
import de.dfki.tocalog.output.Output;
import de.dfki.tocalog.output.SpeechOutput;
import de.dfki.tocalog.output.TextOutput;
import de.dfki.tocalog.output.impp.*;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaResponse;
import de.dfki.tocalog.telegram.TelegramBot;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 */
public class MainYK {
    public static void main(String[] args) throws IOException, InterruptedException {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
//        statechart(args);
//        impp(args);
        framework(args);
    }

    public static void impp(String[] args) {
        EKnowledgeMap<Service> services = new EKnowledgeMap<>();
        services.put("c1", Service.create().setId("c1").setType("console"));

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

    public static void framework(String[] args) throws InterruptedException, IOException {

        TelegramBot tbot = new TelegramBot();

        PSBridge psBridge = PSBridge.build()
                .subscribe("SheepEvent")
                .subscribe("BinaryEvent")
                .build();

        MetaDialog dialog = new MetaDialog();
        dialog.addDialogComponent(new GreetingBehavior());
        dialog.addDialogComponent(new DeviceControlBehavior());

        ProjectManager dc = ProjectManager.create(dialog)
//                .addInputComponent(psBridge)
                .addInputComponent(tbot)
                .addOutputComponent(tbot)
                .addInputComponent(new RasaIntentProducer())
                .build();

        EKnowledgeMap<Triple> focuskm = dc.getKnowledgeBase().getKnowledgeMap(Triple.class, "VisualFocus");
        focuskm.add(Triple.create().setSubject("mechanic1").setPredicate("focus").setObject("car")
                .setSource("kinect").setConfidence(0.7));
        focuskm.add(Triple.create().setSubject("mechanic1").setPredicate("focus").setObject("bike")
                .setSource("hololens").setConfidence(0.4));


        EKnowledgeMap<Triple> gesturekm = dc.getKnowledgeBase().getKnowledgeMap(Triple.class, "mechanic1.Gesture");
        focuskm.add(Triple.create().setSubject("mechanic1").setPredicate("gesture").setObject("SwipeUp")
                .setSource("kinect"));
        focuskm.add(Triple.create().setSubject("mechanic1").setPredicate("gesture").setObject("SwipeLeft")
                .setSource("hololens"));

//        EKnowledgeMap<Service> skm = dc.getKnowledgeBase().getKnowledgeMap(Service.class);
//        skm.add(Service.create().set);


        gesturekm.removeOld(5000L);
        if (gesturekm.getAny(t -> t.getObject().orElse("").equals("car")).isPresent()) {

        }

        Thread timeThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    continue;
                }

                Person p = Person.create()
                        .setId("mechanic1")
                        .setName("der mechaniker");
//                ks.put(p);
                dc.getEventEngine().submit(Event.build(p).build());
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();


        tbot.start();

        dc.getEventEngine().submit(() -> System.out.println("hallo from event queue"));
        dc.run();
    }


    public static void statechart(String[] args) throws IOException {
        State stateA = State.create("A")
                .onEntry(() -> System.out.println("entered A!"))
                .onExit(() -> {

                })
                .build();

        State stateB = State.create("B").build();
        State stateC = State.create("C").build();
        State stateD = State.create("D").build();
        State stateHello = State.create("Goal").build();


        Transition helloSaid = new Transition("hello", stateA, stateB) {
            @Override
            public boolean fires(StateChartEvent eve) {
                return false;
            }
        };


        StateChart sc = StateChart.create()
                .setInitialState(stateA)
                .addTransition(helloSaid)
//                .addTransition("out hello", stateB, stateA, e -> {
//                    return false;
//                })
//                .addTransition("finish", stateB, stateC, t2)
//                .addTransition("D", stateC, stateD, t2)
//                .addTransition("back", stateD, stateA, t2)
//                .addTransition("test", stateD, stateHello, helloSaid)
                .build();


        String mmGraph = sc.toMermaid();
        FileUtils.writeStringToFile(new File("~/tmp/graph.txt"), mmGraph, Charset.defaultCharset());
    }


}
